package org.cybnity.application.accesscontrol.ui.system.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.StreamMessage;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import org.cybnity.application.accesscontrol.ui.api.event.DomainEventType;
import org.cybnity.application.accesscontrol.ui.api.event.TenantRegistrationAttributeName;
import org.cybnity.application.accesscontrol.ui.system.backend.AbstractChannelMessageRouter;
import org.cybnity.application.accesscontrol.ui.system.backend.routing.CollaborationChannel;
import org.cybnity.application.accesscontrol.ui.system.backend.routing.GatewayRoutingPlan;
import org.cybnity.framework.Context;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.*;
import org.cybnity.framework.domain.event.DomainEventFactory;
import org.cybnity.framework.domain.infrastructure.MessageHeader;
import org.cybnity.framework.domain.model.DomainEntity;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.MessageMapper;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.Stream;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.UISAdapter;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.MessageMapperFactory;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.UISAdapterRedisImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Public API service managing the messages supported by a domain.
 * This public exposed service does not apply security control rules and is integrated with Users Interactions Space to deliver the response to the caller of the Event bus.
 * This component life cycle is based on Vert.x loop executed by this thread context.
 * This component ensure control of any message structure before to be delegated to the UI capability domain.
 * <p>
 * This implements the Content-Based Router pattern where the recipient channel is identified from the message
 * content to forward. It's an implementation of architectural pattern named "Content-Based Router".
 * <p>
 * Including the use of a RecipientList helper, this implementation inspect incoming message, determine a list of desired recipients (one or several), and forward the message to all channels associated with the recipients list.
 */
public class DomainPublicAPIMessagesContentBasedRouter extends AbstractChannelMessageRouter {

    /**
     * Client managing interactions with Users Interactions Space.
     */
    private final UISAdapter uisClient;

    /**
     * Technical logging
     */
    private static final Logger logger = Logger.getLogger(DomainPublicAPIMessagesContentBasedRouter.class.getName());

    /**
     * Routing map between Event bus path and UIS channels
     */
    private final GatewayRoutingPlan destinationMap = new GatewayRoutingPlan();

    /**
     * Event bus channel monitored by this worker.
     */
    private final CollaborationChannel consumedChannel = CollaborationChannel.ac_in;

    /**
     * Collection of channels consumers (observing Event bus entry items) managed by this worker.
     */
    private final Collection<MessageConsumer<Object>> entryPointChannelConsumers = new ArrayList<>();

    /**
     * Default constructor.
     *
     * @throws UnoperationalStateException When problem of context configuration (e.g missing environment variable defined to join the Users Interactions Space).
     */
    public DomainPublicAPIMessagesContentBasedRouter() throws UnoperationalStateException {
        try {
            // Prepare client configured for interactions with the UIS
            // according to the defined environment variables (autonomous connection from worker to UIS)
            // defined on the runtime server executing this worker
            uisClient = new UISAdapterRedisImpl(new Context() /* Current context of adapter runtime*/);
        } catch (IllegalArgumentException iae) {
            // Problem of context read
            throw new UnoperationalStateException(iae);
        }
    }

    /**
     * Start event bus channel as provided api service entrypoint.
     * This method start also the observed User Interactions Space channels allowing to deliver answers of delegated works to the capabilities layer.
     */
    @Override
    protected void startChannelConsumers() {
        // Create UIS observed allowing async response treatment to forward at the service callers
        startUISConsumers();

        // Create each entrypoint channel observed by this worker
        entryPointChannelConsumers.add(vertx.eventBus().consumer(consumedChannel.label(), this::onMessage));
        logger.fine("Event bus channels consumers started with success by worker (workerDeploymentId: " + this.deploymentID() + ")");
    }

    @Override
    protected void stopChannelConsumers() {
        // Stop each entrypoint channel previously observed by this worker
        for (MessageConsumer<Object> consumer : entryPointChannelConsumers) {
            consumer.unregister().onComplete(res -> {
                if (res.failed()) {
                    logger.warning("Event bus channel consumer un-registration failed by worker (workerDeploymentId: " + this.deploymentID() + ")!");
                }
            });
        }
        // Clean consumers set
        entryPointChannelConsumers.clear();

        // Stop the UIS observers
        stopUISConsumers();
        logger.fine("Event bus channels consumers un-registered with success by worker (workerDeploymentId: " + this.deploymentID() + ")");
    }

    /**
     * Stop the handlers that monitored the User Interactions Space.
     */
    private void stopUISConsumers() {
        // TODO créer desabonnement aux topics redis

        logger.fine("UIS consumers stopped with success");
    }

    /**
     * Start the handlers that monitor the User Interactions Space and forward observed events to the event bus.
     */
    private void startUISConsumers() {
        // TODO créer consumers du redis permettant de collecter (sur la base du pattern de nommage des envois réalisés par la méthode onMessage() ) les résultats de traitements délégués auprès du redis

        // Identify the


        // Transform domain event to JSON supported by the UI layer

        // Publish the event JSON version to event bus according to the routing case
        // For example:
        // - output channel dedicated to a replyAddress
        // - output channel dedicated to a correlation id
        // - output channel specific to the domain
        // - public output channel

        logger.fine("UIS consumers started with success");

    }

    /**
     * Default entrypoint processing method executed for each message received via the service channel.
     * This provided service is a long-time running process with sync response to the caller over the event bus.
     *
     * @param message Message to process. Do nothing when null.
     */
    private <T> void onMessage(Message<T> message) {
        if (message != null) {
            // Identify fact event to append on space topic
            JsonObject messageBody = (JsonObject) message.body();
            if (messageBody != null && !messageBody.isEmpty()) {
                try {
                    // Identify received command
                    ObjectMapper mapper = new ObjectMapperBuilder().dateFormat().enableIndentation().preserveOrder(true).build();
                    Command factEvent = mapper.readValue(messageBody.encode(), Command.class);

                    // Identify eventual existing reply address to forward as event's additional header
                    String replyAddress = message.replyAddress();
                    // Define specific additional header regarding reply address specifically listener
                    if (replyAddress != null && !replyAddress.isEmpty()) {
                        factEvent.appendSpecification(new Attribute(MessageHeader.REPLY_ADDRESS_HEADER.name(), replyAddress));
                    }

                    // Identify eventual correlation identifier
                    String correlationId = (factEvent.correlationId() != null) ? factEvent.correlationId().value() : null;
                    if (correlationId == null || correlationId.isEmpty()) {
                        // Search eventual existing correlation id defined into the headers
                        DeliveryOptions options = getDeliveryOptions(message.headers().entries());
                        correlationId = options.getHeaders().get("Correlation-ID");
                        if (correlationId != null && !correlationId.isEmpty()) {
                            // Set correlation id on the fact event
                            factEvent.assignCorrelationId(correlationId);
                        }
                    }

                    // Identify event type to support by a capability domain delegation
                    Attribute eventType = factEvent.type();
                    String factEventTypeName = (eventType != null) ? eventType.value() : null;
                    if (factEventTypeName != null && !factEventTypeName.isEmpty()) {
                        // Detect capability domain path (UIS entrypoint of domain) supporting the identified fact event type

                        // --- Make long-time running process with call to capability domains layer over space ---
                        // Execute command via adapter (WITH AUTO-DETECTION OF STREAM RECIPIENT FROM REQUEST EVENT)
                        String routeRecipientPath = destinationMap.recipient(factEventTypeName);
                        if (routeRecipientPath != null) {
                            Stream domainEndpoint = new Stream(/* Detected capability domain path based on entrypoint supported fact event type */ routeRecipientPath);
                            MessageMapper msgMapper = new MessageMapperFactory().getMapper(factEvent.getClass(), StreamMessage.class);
                            String messageId = uisClient.append(factEvent, domainEndpoint /* Specific stream to feed */, msgMapper);
                            logger.log(Level.FINE, factEventTypeName + " command (messageId: " + messageId + ") appended to '" + domainEndpoint.name() + "' capability domain entrypoint");
                            // --- process delegated to capability domain and eventual response managed by the UIS consumers ---


                            // TODO replace mocked response (and vertx.redis usage if none required) for execution since observer of UIS layer over REDIS adapter library
                            // Positionner en mocked feature unit (vertx indépendant) répondant dans le topic replyAddress car existant
                            // puis écouté par un observer de topic du domaine ac pour forward vers event bus

                            DeliveryOptions options = getDeliveryOptions(message.headers().entries());
                            // Temp mocked response to replace by result build from consumer when received response from redis
                            JsonObject registeredOrganizationEvent = mockedResponse(options.getHeaders().get("Correlation-ID"), messageId);

                            message.reply(registeredOrganizationEvent, options);
                        } else {
                            // The type of event is not supported by any channel and declared route
                            // So event type can be considered as un treatable by the domain
                            // So log shall be notified regarding potential bad usage of public API entrypoint and/or conformity violation
                            onInvalidChannelEntry(factEvent, ConformityViolation.UNSUPPORTED_EVENT_TYPE, consumedChannel.label());
                        }
                    } else {
                        // Invalid fact event type received from the ACL channel
                        // Several potential cause can be managed regarding this situation in terms of security violation
                        // For example:
                        // - development error of command transmission to the right channel
                        // - security attack attempt with bad command send test through any channel for test of entry by any api entry point
                        onInvalidChannelEntry(factEvent, ConformityViolation.UNIDENTIFIED_EVENT_TYPE, consumedChannel.label());
                    }
                } catch (JsonProcessingException jpe) {
                    // Problem of data structure detected regarding the received event which can be bound
                    // Cause: message structure conformity violation
                    onInvalidChannelEntry(messageBody.toString(), ConformityViolation.UNSUPPORTED_MESSAGE_STRUCTURE, consumedChannel.label());
                } catch (Exception ex) {
                    logger.log(Level.WARNING, ex.getMessage());
                }
            }
        }
    }

    /**
     * Execute security control defined for analysis and remediation of a technical problem detected in terms of invalid fact received from the event bus's channels supported by this worker.
     *
     * @param received    Origin received event.
     * @param rejectCause Pre-identified cause of invalidity.
     * @param entrypoint  Entrypoint of received command.
     */
    private void onInvalidChannelEntry(Command received, ConformityViolation rejectCause, String entrypoint) {
        onInvalidChannelEntry(received.toString(), rejectCause, entrypoint);
    }

    /**
     * Execute security control defined for analysis and remediation of a technical problem detected in terms of invalid data received from the event bus's channels supported by this worker.
     *
     * @param receivedData Origin received data.
     * @param rejectCause  Pre-identified cause of invalidity.
     * @param entrypoint   Entrypoint of received command.
     */
    private void onInvalidChannelEntry(String receivedData, ConformityViolation rejectCause, String entrypoint) {
        if (receivedData != null && rejectCause != null) {
            // Log error for technical analysis by operator (e.g potential configuration error), or interpretation by incident event rules (e.g potential security event) for remediation execution
            logger.log(Level.SEVERE, rejectCause.name());
        }
    }

    /**
     * Prepare a mocked answer to the service caller.
     * TODO TEMPORARY MOCK CODE BEFORE REAL RESPONSE BUILD AND RECEIVED FROM UIS
     *
     * @param originCorrelationId  transaction identifier.
     * @param transmittedMessageId message identifier that have been sent to UIS.
     * @return A response event shareable with the caller.
     */
    private JsonObject mockedResponse(String originCorrelationId, String transmittedMessageId) {
        try {
            ObjectMapper mapper = new ObjectMapperBuilder().dateFormat().enableIndentation().preserveOrder(true).build();

            // Prepare json object (OrganizationRegistered domain event including state) as
            // simulated UIS answer with success registration
            Collection<Attribute> changeEventDefinition = new ArrayList<>();
            Attribute correlationIdAtt = new Attribute(Command.CORRELATION_ID, originCorrelationId);
            // Set organization name created
            Attribute tenantNameToRegister = new Attribute(TenantRegistrationAttributeName.TENANT_NAMING.name(), "CYBNITY");
            changeEventDefinition.add(tenantNameToRegister);
            // Set correlation id
            changeEventDefinition.add(correlationIdAtt);// allowing finalized transaction check
            // Prepare OrganizationRegistered domain event coming from the UI capabilities layer and forwarded by the UI API
            // including a status equals to "actioned" (e.g waiting for user registration finalized with success)
            // and tenant description
            Collection<Attribute> tenantDefinition = new ArrayList<>();
            tenantDefinition.add(new Attribute(TenantRegistrationAttributeName.TENANT_NAMING.name(), "CYBNITY"));
            // Set correlation id
            tenantDefinition.add(correlationIdAtt); // allowing finalized transaction check

            DomainEvent createdTenantEvent = DomainEventFactory.create(org.cybnity.framework.domain.event.DomainEventType.TENANT_CREATED.name(),
                    new DomainEntity(IdentifierStringBased.generate(null)),
                    tenantDefinition, /* prior as command event entity reference */ null
                    , null);

            DomainEvent changeEvent = DomainEventFactory.create(DomainEventType.TENANT_REGISTERED.name(),
                    /* Identifier of the event to prepare */ null, changeEventDefinition,
                    /* Prior command cause of change*/ createdTenantEvent.getIdentifiedBy().reference(),
                    /* None pre-identified organization because new creation */ null);

            // Transform event into vertx supported JsonObject type allowing binding
            return new JsonObject(mapper.writeValueAsString(changeEvent));
        } catch (Exception e) {
            logger.log(Level.FINE, e.toString());
        }
        return null;
    }

    /**
     * Build and return options supporting the delivery of event over event bus.
     *
     * @param headers Original headers to read for extraction of essential values.
     * @return Options instance including "Correlation-ID" and "Content-Type" headers.
     */
    private DeliveryOptions getDeliveryOptions(List<Map.Entry<String, String>> headers) {
        DeliveryOptions options = null;
        if (headers != null) {
            options = new DeliveryOptions();
            for (Map.Entry<String, String> att : headers) {
                if ("Correlation-ID".equalsIgnoreCase(att.getKey())) {
                    /* X-Request-ID, X-Correlation-ID or Correlation-ID common non-standard request fields */
                    options.addHeader("Correlation-ID", att.getValue());
                } else if ("Content-Type".equalsIgnoreCase(att.getKey())) {
                    options.addHeader("Content-Type", att.getValue());
                }
            }
        }
        return options;
    }

}
