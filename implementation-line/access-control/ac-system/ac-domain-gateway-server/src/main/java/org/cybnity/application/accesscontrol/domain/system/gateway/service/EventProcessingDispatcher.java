package org.cybnity.application.accesscontrol.domain.system.gateway.service;

import org.cybnity.application.accesscontrol.domain.system.gateway.routing.ProcessingUnitAnnouncesObserver;
import org.cybnity.framework.domain.ConformityViolation;
import org.cybnity.framework.domain.IDescribed;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.Stream;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.UISAdapter;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.MessageMapperFactory;

import java.util.logging.Level;

/**
 * Responsible to dispatch filtered event (e.g from domain capability IO entrypoint) to performers (e.g local or remote UI capability processor, or application domain).
 * When an event dispatcher receives an event, it obtains a performer (dynamic assignment according to declared event support) and delegate the event processing to it.
 * A performer can be a dedicated processing unit ensuring capability or embedded processing unit ensuring the event applicative treatment.
 * The performer could be newly created by the dispatcher (e.g service layer executed in embedded mode), or could be selected from a pool of available performers.
 * Each performer can run in its own thread to process events concurrently.
 * All performers may be appropriate for all events, or the dispatcher may match an event to a specialized performer based on properties of the message.
 */
public class EventProcessingDispatcher extends FactBaseHandler {

    /**
     * Origin entrypoint of API under selective pattern.
     */
    private final Stream receivedFrom;
    private final ProcessingUnitAnnouncesObserver dynamicRecipientsListManager;
    private final UISAdapter uisClient;
    /**
     * Mapper factory allowing translation of domain's event and/or message to recipient.
     */
    private final MessageMapperFactory mapperFactory;

    /**
     * Default constructor.
     *
     * @param receivedFrom                 Mandatory API entrypoint of collecting events to filter.
     * @param dynamicRecipientsListManager Mandatory manager of dynamic declared computation units ready for treatment of event types.
     * @param uisClient                    Mandatory operational client to UIS.
     * @param mapperFactory                Mandatory mapper factory supporting the serialization/deserialization of event and messages supported by the delegate to transmit.
     * @throws IllegalArgumentException When required parameter is missing.
     */
    public EventProcessingDispatcher(Stream receivedFrom, ProcessingUnitAnnouncesObserver dynamicRecipientsListManager, UISAdapter uisClient, MessageMapperFactory mapperFactory) throws IllegalArgumentException {
        super();
        if (receivedFrom == null) throw new IllegalArgumentException("ReceivedFrom parameter is required!");
        if (dynamicRecipientsListManager == null)
            throw new IllegalArgumentException("dynamicRecipientsListManager parameter is required!");
        if (uisClient == null) throw new IllegalArgumentException("uisClient parameter is required!");
        if (mapperFactory == null) throw new IllegalArgumentException("mapperFactory parameter is required!");
        this.receivedFrom = receivedFrom;
        this.dynamicRecipientsListManager = dynamicRecipientsListManager;
        this.uisClient = uisClient;
        this.mapperFactory = mapperFactory;
    }

    @Override
    public boolean process(IDescribed fact) {
        if (canHandle(fact)) {
            // Event type able to be processed by a type of PU
            ProcessingUnitDelegation processingDelegation = new RemoteProcessingUnitExecutor(dynamicRecipientsListManager, uisClient, mapperFactory);
            // Execute the delegation process supporting the event treatment
            processingDelegation.process(fact);
            return true;
        } else {
            // Invalid fact event type received
            // Several potential cause can be managed regarding this situation in terms of security violation
            // For example:
            // - development error of command transmission to the right stream
            // - security attack attempt with bad command send test through any channel for test of entry by any capability api entry point
            logger().log(Level.SEVERE, ConformityViolation.UNIDENTIFIED_EVENT_TYPE.name() + ": invalid fact type (" + fact.type() + ") received from channel (" + receivedFrom.name() + ")!");
        }
        return false; // Interrupt next step activation
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

}
