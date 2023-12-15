package org.cybnity.application.accesscontrol.domain.system.gateway.service;

import org.cybnity.application.accesscontrol.ui.api.event.CommandName;
import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.ConformityViolation;
import org.cybnity.framework.domain.IDescribed;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.Channel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

/**
 * Selective Consumer (EI pattern), one that filteres the messages delivered by a channel so that it only receives the ones that match its criteria.
 * <p>
 * There are three parts to this filtering process:
 * Specifying Producer — Specifies the event’s selection value before sending it.
 * Selection Value — One or more values specified in the event that allow to decide whether to select the event.
 * Selective Consumer — Only receives event that meet its selection criteria.
 */
public class APISupportedCapabilitySelectionFilter extends FactBaseHandler {

    /**
     * Referential of criteria that allow (e.g by evaluation as filtering pattern) to select or not the event as supported by an API.
     * Each item is defined by an event type name (key) and a UIS recipient path (value identifying a topic/stream name).
     */
    private Collection<String> selectionCriteria;

    /**
     * Origin entrypoint of API under selective pattern.
     */
    private final Channel receivedFrom;

    /**
     * Default constructor.
     *
     * @param receivedFrom Mandatory API entrypoint of collecting events to filter.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public APISupportedCapabilitySelectionFilter(Channel receivedFrom) throws IllegalArgumentException {
        super();
        if (receivedFrom == null) throw new IllegalArgumentException("ReceivedFrom parameter is required!");
        this.receivedFrom = receivedFrom;
        initCommandEventSupportedByAPI();
    }

    /**
     * Define the static referential of command or domain event types that are supported and processed by the API.
     */
    private void initCommandEventSupportedByAPI() {
        selectionCriteria = new ArrayList<>();
        // Add all the command types supported by the API
        selectionCriteria.add(CommandName.REGISTER_ORGANIZATION.name());

        // Add all the domain event type supported by the API
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
                if (selectionCriteria.contains(factEventTypeName)) {
                    // The command is supported by the api
                    // So can continue the processing pipeline
                    return true; // Confirm next step activation
                } else {
                    // Event shall be ignored because not supported by this API
                    // It's a violation of API requirements (e.g technical error by routing map)
                    // or it's a potential security event (e.g attempt of penetration of invalid data into the API)
                    // Log error for technical analysis by operator and remediation execution
                    logger().log(Level.SEVERE, errorMsg.toString());
                }
            } else {
                // Invalid structure of received event
                logger().log(Level.SEVERE, errorMsg.toString());
            }
        } else {
            // Invalid fact event type received
            // Several potential cause can be managed regarding this situation in terms of security violation
            // For example:
            // - development error of command transmission to the right stream
            // - security attack attempt with bad command send test through any channel for test of entry by any capability api entry point
            logger().log(Level.SEVERE, ConformityViolation.UNIDENTIFIED_EVENT_TYPE.name() + ": invalid fact type received from channel (" + receivedFrom.name() + ")!");
        }
        return false; // Interrupt next step activation
    }
}
