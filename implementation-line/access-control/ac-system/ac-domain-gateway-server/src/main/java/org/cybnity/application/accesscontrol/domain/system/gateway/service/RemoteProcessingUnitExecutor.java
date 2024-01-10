package org.cybnity.application.accesscontrol.domain.system.gateway.service;

import io.lettuce.core.StreamMessage;
import org.cybnity.framework.application.vertx.common.routing.IEventProcessingManager;
import org.cybnity.framework.application.vertx.common.routing.UISRecipientList;
import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.ConformityViolation;
import org.cybnity.framework.domain.IDescribed;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.MappingException;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.Stream;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.UISAdapter;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.MessageMapperFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processor ensuring a transfer of event to process (as a Proxy delegate) the treatment relative to an event.
 * For example, can be responsible to forward an event to a remote UI capability over a middleware stream.
 */
public class RemoteProcessingUnitExecutor implements ProcessingUnitDelegation {

    private final IEventProcessingManager recipientsProvider;
    private final UISAdapter uisClient;
    /**
     * Technical logging
     */
    private static final Logger logger = Logger.getLogger(RemoteProcessingUnitExecutor.class.getName());

    /**
     * Mapper factory allowing translation of domain's event and/or message to recipient.
     */
    private MessageMapperFactory mapperFactory;

    /**
     * Default constructor.
     *
     * @param recipientsProvider Mandatory manager of dynamic or static recipients able to be delegated of event processing.
     * @param uisClient          Mandatory operational client connected to UIS.
     * @param eventMapperFactory Mandatory mapper factory supporting the serialization/deserialization of event and messages supported by the delegate to transmit.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public RemoteProcessingUnitExecutor(IEventProcessingManager recipientsProvider, UISAdapter uisClient, MessageMapperFactory eventMapperFactory) throws IllegalArgumentException {
        super();
        if (recipientsProvider == null) throw new IllegalArgumentException("recipientsProvider parameter is required!");
        if (uisClient == null) throw new IllegalArgumentException("uisClient parameter is required!");
        if (eventMapperFactory == null) throw new IllegalArgumentException("eventMapperFactory parameter is required!");
        this.recipientsProvider = recipientsProvider;
        this.uisClient = uisClient;
        this.mapperFactory = eventMapperFactory;
    }

    @Override
    public void process(IDescribed factEvent) throws IllegalArgumentException {
        if (factEvent == null) throw new IllegalArgumentException("factEvent parameter is required!");
        // Identify mapping key about supported event type name
        Attribute factType = factEvent.type();
        if (factType != null) {
            String eventTypeName = factEvent.type().value();
            // Identify existing path (e.g UIS stream recipient dynamically updated according to the started remote IService providers) to the remote service component as able to treat the event
            // based on DynamicRecipientList pattern implementation according to the fact event type name
            UISRecipientList destinationMap = recipientsProvider.delegateDestinations();
            String PUEntrypointChannel = destinationMap.recipient(eventTypeName);
            if (PUEntrypointChannel != null) {
                try {
                    Stream domainEndpoint = new Stream(/* Detected capability domain path based on entrypoint supported fact event type */ destinationMap.recipient(eventTypeName));

                    String messageId = uisClient.append(factEvent, domainEndpoint /* Specific stream to feed */, /* Get a mapper supporting the event type and message */ mapperFactory.getMapper(factEvent.getClass(), StreamMessage.class));
                    logger.log(Level.FINE, eventTypeName + " fact event (messageId: " + messageId + ") appended to '" + domainEndpoint.name() + "' capability domain entrypoint");
                    // --- process delegated to capability domain and eventual response managed by the UIS consumers ---
                } catch (MappingException jme) {
                    logger.log(Level.SEVERE, ConformityViolation.UNPROCESSABLE_EVENT_TYPE.name() + ": invalid fact type (" + eventTypeName + ") mapped for processing delegation attempt!");
                }
            } else {
                // None processing unit is defined as able to perform the event treatment (e.g non started and announced into the dynamic routing map)
                logger.log(Level.SEVERE, ConformityViolation.UNPROCESSABLE_EVENT_TYPE.name() + ": none processing unit destination is currently dynamically identified as able to treat the fact event (" + eventTypeName + ")!");
            }
        } else {
            // Impossible to identify the processing unit from undefined/unknown event type
            // Create log about conformity violation
            logger.log(Level.SEVERE, ConformityViolation.UNPROCESSABLE_EVENT_TYPE.name() + ": impossible identification of fact type name required for its processing delegation!");
        }
    }

}
