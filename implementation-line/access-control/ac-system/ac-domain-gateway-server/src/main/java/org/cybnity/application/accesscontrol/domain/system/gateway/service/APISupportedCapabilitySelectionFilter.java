package org.cybnity.application.accesscontrol.domain.system.gateway.service;

import org.cybnity.framework.application.vertx.common.routing.IEventProcessingManager;
import org.cybnity.framework.application.vertx.common.routing.RouteRecipientList;
import org.cybnity.framework.application.vertx.common.service.AbstractServiceActivator;
import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.ConformityViolation;
import org.cybnity.framework.domain.IDescribed;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.Stream;

import java.util.logging.Level;

/**
 * Selective Consumer (EI pattern), one that filter the messages delivered by a channel so that it only receives the ones that match its criteria.
 * <p>
 * There are three parts to this filtering process:
 * Specifying Producer — Specifies the event’s selection value before sending it.
 * Selection Value — One or more values specified in the event that allow to decide whether to select the event.
 * Selective Consumer — Only receives event that meet its selection criteria.
 */
public class APISupportedCapabilitySelectionFilter extends AbstractServiceActivator {

    /**
     * Origin entrypoint of API under selective pattern.
     */
    private final Stream receivedFrom;

    /**
     * Provider of referential criteria that allow (e.g by evaluation as filtering pattern) to select or not the event as supported by an API.
     * Each item is defined by an event type name (key) and a UIS recipient path (value identifying a topic/stream name).
     */
    private final IEventProcessingManager eventTypesProvider;

    /**
     * Default constructor.
     *
     * @param receivedFrom                  Mandatory API entrypoint of collecting events to filter.
     * @param supportableEventTypesProvider Mandatory provider of event types supportable by the API.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public APISupportedCapabilitySelectionFilter(Stream receivedFrom, IEventProcessingManager supportableEventTypesProvider) throws IllegalArgumentException {
        super();
        if (receivedFrom == null) throw new IllegalArgumentException("ReceivedFrom parameter is required!");
        if (supportableEventTypesProvider == null)
            throw new IllegalArgumentException("recipientsProvider parameter is required!");
        this.receivedFrom = receivedFrom;
        this.eventTypesProvider = supportableEventTypesProvider;
    }

    /**
     * Check that is defined fact.
     *
     * @param fact Fact to identify.
     * @return True when fact parameter is not null.
     */
    @Override
    protected boolean canHandle(IDescribed fact) {
        return fact != null;
    }

    /**
     * Perform the evaluation of the fact to determine if it shall be ignored (e.g because not supported by the API, with or without exception thrown).
     * This realization analyze the event content (e.g supported event type name) and identify received event as supported by the capability domain.
     * When supported event is detected,
     *
     * @param fact To process.
     * @return True when responsibility chain can be continued. Else false when chain shall be interrupted.
     */
    @Override
    public boolean process(IDescribed fact) {
        if (canHandle(fact)) {
            // Identify event type
            Attribute eventType = fact.type();
            String factEventTypeName = (eventType != null && !eventType.value().isEmpty()) ? eventType.value() : null;
            StringBuilder errorMsg = new StringBuilder().append(ConformityViolation.UNIDENTIFIED_EVENT_TYPE.name())
                    .append(": invalid fact type (")
                    .append(factEventTypeName).append(") received into the channel (")
                    .append(receivedFrom.name()).append("), that can't be processed and have been ignored!");

            if (factEventTypeName != null) {
                // Check if command event is supported by the API and shall be processed
                // From dynamic routing plan
                RouteRecipientList destinationMap = eventTypesProvider.delegateDestinations();

                if (destinationMap.supportedEventTypeNames().contains(factEventTypeName)) {
                    // The command is supported by the api
                    // So can continue the processing pipeline
                    return true; // Confirm next step activation
                } else {
                    // Event shall be ignored because not supported by this API
                    // Move it to Invalid Message Channel
                    moveToInvalidMessageChannel(fact, errorMsg.toString());
                }
            } else {
                // Invalid structure of received event
                moveToInvalidMessageChannel(fact, errorMsg.toString());
            }
        } else {
            // Invalid fact event type received
            moveToInvalidMessageChannel(fact, ConformityViolation.UNIDENTIFIED_EVENT_TYPE.name() + ": invalid fact type received from channel (" + receivedFrom.name() + ")!");
        }
        return false; // Interrupt next step activation
    }


    @Override
    protected void moveToInvalidMessageChannel(IDescribed unprocessedEvent, String cause) {
        // It's a violation of API requirements (e.g technical error by routing map)
        // or it's a potential security event (e.g attempt of penetration of invalid data into the API)

        // Several potential cause can be managed regarding this situation in terms of security violation
        // For example:
        // - development error of command transmission to the right stream
        // - security attack attempt with bad command send test through any channel for test of entry by any capability api entry point

        // Log error for technical analysis by operator and remediation execution
        logger().log(Level.SEVERE, cause);
    }

    @Override
    protected void moveToDeadLetterChannel(IDescribed unprocessedEvent, String cause) {

    }
}
