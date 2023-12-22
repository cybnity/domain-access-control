package org.cybnity.application.accesscontrol.domain.system.gateway.routing;

import org.cybnity.application.accesscontrol.ui.api.UICapabilityChannel;
import org.cybnity.application.accesscontrol.ui.api.event.AttributeName;
import org.cybnity.application.accesscontrol.ui.api.event.CollaborationEventType;
import org.cybnity.application.accesscontrol.ui.api.routing.UISRecipientList;
import org.cybnity.framework.domain.*;
import org.cybnity.framework.domain.event.DomainEventFactory;
import org.cybnity.framework.domain.event.EventSpecification;
import org.cybnity.framework.domain.event.ProcessingUnitPresenceAnnounced;
import org.cybnity.framework.domain.model.DomainEntity;
import org.cybnity.framework.immutable.BaseConstants;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.framework.immutable.ImmutabilityException;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.Channel;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.ChannelObserver;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.UISAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.logging.Logger;

public class ProcessingUnitAnnouncesObserver implements ChannelObserver, IEventProcessingManager {

    /**
     * Logical name of the component managing routing paths.
     */
    private final String dynamicRoutingServiceName;

    /**
     * Technical logging
     */
    private static final Logger logger = Logger.getLogger(ProcessingUnitAnnouncesObserver.class.getName());

    /**
     * Gateway control channel allowing to receive the computation unit (e.g UI capability standalone components) announces of presence which can be registered as eligible execution recipients for event types treatment.
     * It's a control channel of this router (Dynamic Router pattern) that can self-configure based on special configuration messages from participating destinations (e.g other independent UI capability components ready for processing of event over the UIS).
     */
    private final Channel dynamicRoutersControlChannel;

    /**
     * Map of announced processing units.
     * Identify existing path (e.g UIS stream recipient) to the remote service components which are eligible as delegate for event treatment
     * based on RecipientList pattern implementation according to the fact event type name
     */
    private final UISRecipientList delegatesDestinationMap = new UISRecipientList();

    /**
     * Client to Users Interactions Space allowing notifications of changes regarding the routing plan.
     */
    private final UISAdapter uisClient;

    /**
     * Topic channel(s) where dynamic routing path change events are promoted.
     */
    private final Collection<Channel> registeredRoutingPathChange;

    /**
     * Default constructor.
     *
     * @param dynamicRoutersControlChannel Mandatory control channel that shall be observed to be notified of processing unit presence announces.
     * @param dynamicRoutingServiceName    Optional name regarding the logical component which own this routing service.
     * @param uisClient                    Optional Users Interactions Space adapter usable for notification of registered routes over UIS channel. When null, the recipients list updated are not notified.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public ProcessingUnitAnnouncesObserver(Channel dynamicRoutersControlChannel, String dynamicRoutingServiceName, UISAdapter uisClient) throws IllegalArgumentException {
        if (dynamicRoutersControlChannel == null)
            throw new IllegalArgumentException("Dynamic routers control channel parameter is required!");
        this.dynamicRoutersControlChannel = dynamicRoutersControlChannel;
        this.dynamicRoutingServiceName = dynamicRoutingServiceName;
        this.uisClient = uisClient;

        // Define the collection of topics where recipients list changes shall be promoted
        registeredRoutingPathChange = new ArrayList<>();
        registeredRoutingPathChange.add(new Channel(UICapabilityChannel.access_control_io_gateway_dynamic_routing_plan_evolution.shortName()));
    }

    /**
     * Get the control channel received the announces of presence declared by the processing units eligible as event treatment delegates.
     *
     * @return A channel.
     */
    @Override
    public Channel observed() {
        return dynamicRoutersControlChannel;
    }

    @Override
    public String observationPattern() {
        return null;
    }

    @Override
    public void notify(DomainEvent domainEvent) {
        if (domainEvent instanceof ProcessingUnitPresenceAnnounced) {
            ProcessingUnitPresenceAnnounced event = (ProcessingUnitPresenceAnnounced) domainEvent;
            // It's an event promoted by a processing unit regarding its presence and availability for delegation of event treatment
            Collection<Attribute> eventsRoutingPathsCollection = event.eventsRoutingPaths();

            // Register the paths per supported event type into the dynamic controlled recipients list
            String recipientPath;
            String supportedEventTypeName;
            boolean changedRecipientsContainer = false;
            for (Attribute routeDefinition : eventsRoutingPathsCollection) {
                supportedEventTypeName = routeDefinition.name();
                recipientPath = routeDefinition.value();
                if (supportedEventTypeName != null && !supportedEventTypeName.isEmpty()) {
                    // Update the dynamic recipient list regarding event type definition
                    // (add of new path, upgrade of existing path, deletion of previous path)
                    if (delegatesDestinationMap.addRoute(supportedEventTypeName.trim(), recipientPath)) {
                        changedRecipientsContainer = true;
                    }
                }
            }

            // Notify confirmed dynamic recipients list changes
            if (changedRecipientsContainer) notifyDynamicRecipientListChanged(domainEvent, uisClient);
        } else {
            // Invalid type of notification event received into the control channel
            logger.severe("Reception of invalid event type into the control channel (" + observed().name() + ") which shall only receive " + ProcessingUnitPresenceAnnounced.class.getSimpleName() + " supported event!");
        }
    }

    /**
     * Notify the changed recipients list regarding delegation path registered to a processing unit.
     * The notifications are only performed when UIS client is defined.
     *
     * @param origin Original cause of routing map change. When null, none notification promoted.
     * @param client Adapter of UIS to feed with CollaborationEventType.PROCESSING_UNIT_ROUTING_PATH_REGISTERED event notification. When null, none notification promoted.
     */
    private void notifyDynamicRecipientListChanged(DomainEvent origin, UISAdapter client) {
        if (client != null && origin != null) {
            // Notify registered routing map to service managed topic's observers
            try {
                LinkedHashSet<Identifier> childEventIdentifiers = new LinkedHashSet<>();
                // Define a unique identifier of the new event
                childEventIdentifiers.add(new IdentifierStringBased(BaseConstants.IDENTIFIER_ID.name(), UUID.randomUUID().toString()));
                // Reference parent identifier origin of this fact
                Identifier parentId = origin.identified();
                if (parentId != null)
                    childEventIdentifiers.add((Identifier) parentId.immutable());
                DomainEntity identifiedBy = new DomainEntity(childEventIdentifiers);

                // Define optional descriptions relative to the gateway notifying the routing map management
                Collection<Attribute> specification = new ArrayList<>();
                if (this.dynamicRoutingServiceName != null && !this.dynamicRoutingServiceName.isEmpty())
                    // Owner of the dynamic routing service
                    EventSpecification.appendSpecification(new Attribute(AttributeName.ServiceName.name(), dynamicRoutingServiceName), specification);

                // Original path of received registration request
                EventSpecification.appendSpecification(new Attribute(AttributeName.SourceChannelName.name(), dynamicRoutersControlChannel.name()), specification);

                // Optional origin event's correlationId if defined (e.g allowing a transactional response interpretation by the subscriber that previously notified its proposal as processing unit)
                Attribute correlationId = origin.correlationId();
                if (correlationId != null) {
                    EventSpecification.appendSpecification(correlationId, specification);
                }

                // Create change event and publish notification on channel(s) potential observed by other domain components (e.g PU which updated the API recipients list about its delegation routing plan)
                client.publish(DomainEventFactory.create(CollaborationEventType.PROCESSING_UNIT_ROUTING_PATHS_REGISTERED.name(), identifiedBy, specification, /* priorCommandRef */ origin.reference(), null /* domain changedModelElementRef */), registeredRoutingPathChange);
            } catch (ImmutabilityException ie) {
                logger.warning(ie.getMessage());
            } catch (Exception e) {
                // Problem during the publishing of change event to UIS channel(s)
            }
        }
    }

    @Override
    public void notify(Command command) {
        if (command != null) {
            logger.warning(ConformityViolation.UNSUPPORTED_MESSAGE_STRUCTURE.name() + ": command event shall not be received from the processing unit presence announces channel (" + observed().name() + ")!");
        }
    }

    @Override
    public UISRecipientList delegateDestinations() {
        return delegatesDestinationMap;
    }
}
