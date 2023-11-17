package org.cybnity.application.accesscontrol.ui.system.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.cybnity.application.accesscontrol.ui.api.event.AttributeName;
import org.cybnity.application.accesscontrol.ui.api.event.DomainEventType;
import org.cybnity.application.accesscontrol.ui.system.backend.AbstractAccessControlChannelWorker;
import org.cybnity.application.accesscontrol.ui.system.backend.routing.CollaborationChannel;
import org.cybnity.framework.domain.*;
import org.cybnity.framework.domain.event.ConcreteDomainChangeEvent;
import org.cybnity.framework.domain.event.DomainEventFactory;
import org.cybnity.framework.domain.model.DomainEntity;

import java.util.*;

/**
 * Public API service managing the registration of an organization as tenant.
 * This public exposed service does not apply access control rules and is integrated with Users Interactions Space to deliver the response to the caller over the Event bus.
 */
public class PublicOrganizationRegistrationWorker extends AbstractAccessControlChannelWorker {

    /**
     * Start event bus channel as provided api service entre-point.
     * This method start also the observed User Interactions Space channels allowing to deliver answers of delegated works to the capabilities layer.
     */
    @Override
    protected void startChannelConsumers() {
        // Create UIS observed allowing async response treatment to forward at the service callers
        startUISConsumers();
        // Create each entrypoint channel observed by this worker
        vertx.eventBus().consumer(CollaborationChannel.ac_in_public_organization_registration.label(), this::onMessage);
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
    }

    /**
     * Default entrypoint processing method executed for each message received via the service channel.
     * This provided service is a long-time running process with sync response to the caller over the event bus.
     *
     * @param message Message to process. Do nothing when null.
     */
    protected <T> void onMessage(Message<T> message) {
        if (message != null) {
            JsonObject messageBody = (JsonObject) message.body();

            // Make long-time running process with call to UIS capabilities layer

            // Identify eventual existing reply address
            // and/or optional original values (e.g correlation id) to forward over UIS
            String replyAddress = message.replyAddress();
            String originEntrypointChannelAddress = message.address();

            // TODO replace mocked response for execution since observer of UIS layer

            DeliveryOptions options = getDeliveryOptions(message.headers().entries());
            // Temp mocked response to replace by result build from consumer when received response from redis
            JsonObject registeredOrganizationEvent = mockedResponse(options.getHeaders().get("Correlation-ID"));

            message.reply(registeredOrganizationEvent, options);
        }
    }

    /**
     * Prepare a mocked answer to the service caller.
     * TODO TEMPORARY MOCK CODE BEFORE REAL RESPONSE BUILD AND RECEIVED FROM UIS
     *
     * @param originCorrelationId transaction identifier.
     * @return A response event shareable with the caller.
     */
    private JsonObject mockedResponse(String originCorrelationId) {
        try {
            ObjectMapper mapper = new ObjectMapperBuilder().dateFormat().enableIndentation().preserveOrder(true).build();

            // Prepare json object (OrganizationRegistered domain event including state) as
            // simulated UIS answer with success registration
            Collection<Attribute> changeEventDefinition = new ArrayList<>();
            Attribute correlationIdAtt = new Attribute(Command.CORRELATION_ID, originCorrelationId);
            // Set organization name created
            Attribute tenantNameToRegister = new Attribute(AttributeName.OrganizationNaming.name(), "CYBNITY");
            changeEventDefinition.add(tenantNameToRegister);
            // Set correlation id
            changeEventDefinition.add(correlationIdAtt);// allowing finalized transaction check
            // Prepare OrganizationRegistered domain event coming from the UI capabilities layer and forwarded by the UI API
            // including a status equals to "actioned" (e.g waiting for user registration finalized with success)
            // and tenant description
            Collection<Attribute> tenantDefinition = new ArrayList<>();
            tenantDefinition.add(new Attribute(AttributeName.OrganizationNaming.name(), "CYBNITY"));
            // Set correlation id
            tenantDefinition.add(correlationIdAtt); // allowing finalized transaction check

            DomainEvent createdTenantEvent = DomainEventFactory.create(DomainEventType.TENANT_CREATED.name(),
                    new DomainEntity(new IdentifierStringBased("id", UUID.randomUUID().toString())),
                    tenantDefinition, /* prior as command event entity reference */ null
                    , null);

            ConcreteDomainChangeEvent changeEvent = (ConcreteDomainChangeEvent) DomainEventFactory.create(DomainEventType.ORGANIZATION_REGISTERED.name(),
                    /* Identifier of the event to prepare */ null, changeEventDefinition,
                    /* Prior command cause of change*/ createdTenantEvent.getIdentifiedBy().reference(),
                    /* None pre-identified organization because new creation */ null);

            // Transform event into vertx supported JsonObject type allowing binding
            return new JsonObject(mapper.writeValueAsString(changeEvent));
        } catch (Exception e) {
            // TODO change for logging tool usage
            e.printStackTrace();
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
