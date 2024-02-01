package org.cybnity.application.accesscontrol.domain.system.gateway.service;

import io.lettuce.core.StreamMessage;
import org.cybnity.application.accesscontrol.translator.ui.api.ACDomainMessageMapperFactory;
import org.cybnity.application.accesscontrol.ui.api.UICapabilityChannel;
import org.cybnity.framework.Context;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.application.vertx.common.AbstractMessageConsumerEndpoint;
import org.cybnity.framework.application.vertx.common.event.AttributeName;
import org.cybnity.framework.application.vertx.common.routing.ProcessingUnitAnnouncesObserver;
import org.cybnity.framework.application.vertx.common.service.FactBaseHandler;
import org.cybnity.framework.domain.*;
import org.cybnity.framework.domain.event.CollaborationEventType;
import org.cybnity.framework.domain.event.CommandFactory;
import org.cybnity.framework.domain.event.CorrelationIdFactory;
import org.cybnity.framework.domain.event.ProcessingUnitPresenceAnnounced;
import org.cybnity.framework.domain.model.DomainEntity;
import org.cybnity.framework.immutable.BaseConstants;
import org.cybnity.framework.immutable.EntityReference;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.framework.immutable.ImmutabilityException;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.*;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.MessageMapperFactory;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.UISAdapterImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Public API service managing the events supported by a domain.
 * It manages execution over in self vertx loop.
 * Use the Pipes and Filters architectural style to divide a larger processing task into a sequence of smaller, independent processing steps (Filters) that are connected by channels (Pipes).
 * Each filter exposes a very simple interface: it receives events on the inbound pipe, processes the message (e.g identify if supported and can be processed by a public or secure performer), and publishes the results to the outbound pipe (e.g delegation to a dedicated capability processor).
 * The pipe connects one filter to the next, sending output messages from one filter to the next.
 * Because all component use the same external interface they can be composed into different solutions by connecting the components to different pipes.
 * We can add new filters, omit existing ones or rearrange them into a new sequence -- all without having to change the filters themselves. The connection between filter and pipe is sometimes called port. In the basic form, each filter component has one input port and one output port.
 */
public class DomainIOEventsPipeline extends AbstractMessageConsumerEndpoint implements StreamObserver, IPresenceObservability {

    /**
     * Functional status based on the operational state of all consumers started and active.
     */
    private PresenceState currentPresenceStatus;

    /**
     * Logical name of the component (reusable in service grid for logical identification when exchanged events between the component and others over the UIS).
     * Optional component name that own the routing service and can be identified in UIS by logical name.
     */
    private static final String DYNAMIC_ROUTING_SERVICE_NAME = NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.GATEWAY, /* domainName */ "ac", /* componentMainFunction */"io",/* resourceType */ null, /* segregationLabel */ null);

    /**
     * Client managing interactions with Users Interactions Space.
     */
    private final UISAdapter uisClient;

    /**
     * Technical logging
     */
    private static final Logger logger = Logger.getLogger(DomainIOEventsPipeline.class.getName());

    /**
     * Logical name of this feature module usable in logs.
     */
    private static final String DOMAIN_PIPELINE_LOGICAL_NAME = "AC domain IO";

    /**
     * UIS entrypoint monitored by this worker.
     */
    private final Stream domainInputChannel = new Stream(UICapabilityChannel.access_control_in.shortName());

    /**
     * Name of the consumers group relative to this pipelined entry points channel.
     */
    private static final String CONSUMERS_GROUP_NAME = NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.PIPELINE, /* domainName */ "ac", /* componentMainFunction */"io",/* resourceType */ null, /* segregationLabel */ "consumers");

    /**
     * Collection of fact event consumers (observing DIS entry items) of streams managed by this worker.
     */
    private final Collection<StreamObserver> entryPointStreamConsumers = new ArrayList<>();

    /**
     * Collection of fact event consumers of pub/sub topics listened by this worker.
     */
    private final Collection<ChannelObserver> topicsConsumers = new ArrayList<>();

    /**
     * Listener of processing units' entry points that can be used by pipeline as delegates for event treatments.
     */
    private ProcessingUnitAnnouncesObserver delegatedExecutionRecipientsAnnouncesStreamConsumer;

    /**
     * IO events pipeline singleton.
     */
    private FactBaseHandler pipelinedProcessSingleton;

    /**
     * Default constructor.
     *
     * @throws UnoperationalStateException When problem of context configuration (e.g missing environment variable defined to join the UIS or DIS).
     */
    public DomainIOEventsPipeline() throws UnoperationalStateException {
        try {
            // Prepare client configured for interactions with the UIS
            // according to the defined environment variables (autonomous connection from worker to UIS)
            // defined on the runtime server executing this worker
            uisClient = new UISAdapterImpl(new Context() /* Current context of adapter runtime*/);
        } catch (IllegalArgumentException iae) {
            // Problem of context read
            throw new UnoperationalStateException(iae);
        }
    }

    @Override
    protected void cleanConsumersResources() {
        // Clean stream consumers set
        entryPointStreamConsumers.clear();
        // Clean channel consumers set
        topicsConsumers.clear();
        // Clean resource allowed by UIS client
        uisClient.freeUpResources();
    }

    /**
     * Enhance the default start process relative to observed channels and/or streams.
     */
    @Override
    public void start() {
        // Tag the current operational and active status
        currentPresenceStatus = PresenceState.AVAILABLE;

        // Execute by default the start operations relative to channels and streams consumers
        super.start();

        // Notify any other component about the processing unit presence in operational status
        try {
            // Promote announce about the supported event types consumed this pipeline
            announcePresence(currentPresenceStatus, /* None previous origin event because it's the first start of this component start or restart */ null);
        } catch (Exception me) {
            // Normally shall never arrive; so notify implementation code issue
            logger.log(Level.SEVERE, me.getMessage(), me);
        }
    }

    /**
     * Enhance the default stop process relative to previously observed channels and/or streams.
     */
    @Override
    public void stop() {
        // Tag the current operational as ended and no active status
        currentPresenceStatus = PresenceState.UNAVAILABLE;

        // Notify any other component about the processing unit presence in end of lifecycle status
        try {
            // Promote announce about the supported event types consumption end by this pipeline
            announcePresence(currentPresenceStatus, null);
        } catch (Exception me) {
            // Normally shall never arrive; so notify implementation code issue
            logger.log(Level.SEVERE, me.getMessage(), me);
        }

        // Execute by default the stop operations relative to channels and streams previously observed
        super.stop();
    }

    /**
     * Start UIS stream as provided api service entrypoint.
     */
    @Override
    protected void startStreamConsumers() {
        // Create each entrypoint stream observed by this worker
        entryPointStreamConsumers.add(this);// Main IO entrypoint observer

        // Define usable mapper supporting the read of stream message received from the Users Interactions Space and translated into domain event types
        MessageMapper eventMapper = getMessageMapperProvider().getMapper(StreamMessage.class, IDescribed.class);

        try {
            // Register all consumers of observed channels
            uisClient.register(entryPointStreamConsumers, eventMapper);
            logger.fine(DOMAIN_PIPELINE_LOGICAL_NAME + " entrypoint stream consumers started with success by worker (workerDeploymentId: " + this.deploymentID() + ")");
        } catch (UnoperationalStateException e) {
            logger.severe(e.getMessage());
        }
    }

    @Override
    protected void startChannelConsumers() {
        // Create entrypoint of delegates presence announces able to dynamically feed the processing unit recipients list
        delegatedExecutionRecipientsAnnouncesStreamConsumer = new ProcessingUnitAnnouncesObserver(/* Where new routes declaration to manage shall be listened */new Channel(UICapabilityChannel.access_control_pu_presence_announcing.shortName()), DYNAMIC_ROUTING_SERVICE_NAME, uisClient,/* Where recipients list changes shall be notified */ new Channel(UICapabilityChannel.access_control_io_gateway_dynamic_routing_plan_evolution.shortName()));
        topicsConsumers.add(delegatedExecutionRecipientsAnnouncesStreamConsumer); // Delegate PU announces observer

        try {
            // Register observers on space
            uisClient.subscribe(topicsConsumers, getMessageMapperProvider().getMapper(String.class, IDescribed.class));
            logger.fine(DOMAIN_PIPELINE_LOGICAL_NAME + " entrypoint channel consumers started with success by worker (workerDeploymentId: " + this.deploymentID() + ")");
        } catch (UnoperationalStateException e) {
            logger.severe(e.getMessage());
        }
    }

    @Override
    protected void stopStreamConsumers() {
        // Stop each entrypoint stream previously observed by this worker
        uisClient.unregister(entryPointStreamConsumers);
        logger.fine(DOMAIN_PIPELINE_LOGICAL_NAME + " entrypoint stream consumers un-registered with success by worker (workerDeploymentId: " + this.deploymentID() + ")");
    }

    @Override
    protected void stopChannelConsumers() {
        // Stop each observed channel by this worker
        uisClient.unsubscribe(topicsConsumers);
        logger.fine(DOMAIN_PIPELINE_LOGICAL_NAME + " gateway consumers unsubscribed with success by worker (workerDeploymentId: " + this.deploymentID() + ")");
    }

    @Override
    public Stream observed() {
        return domainInputChannel;
    }

    /**
     * Collect and treat all the event received from the entrypoint.
     *
     * @return StreamObserver.DEFAULT_OBSERVATION_PATTERN.
     */
    @Override
    public String observationPattern() {
        return StreamObserver.DEFAULT_OBSERVATION_PATTERN;
    }

    @Override
    public String consumerGroupName() {
        return CONSUMERS_GROUP_NAME;
    }

    /**
     * Assembly of the events pipeline steps as a singleton responsibility chain and return it.
     *
     * @return A usable stateless pipelined process.
     */
    private FactBaseHandler pipelinedProcess() {
        if (pipelinedProcessSingleton == null) {
            // Build responsibility chain ensuring the command treatment according to the fact conformity

            // FILTER : identify received command as supported by the capability domain
            APISupportedCapabilitySelectionFilter eventTypeFilteringStep = new APISupportedCapabilitySelectionFilter(this.domainInputChannel);

            // FILTER : select optional authenticator ensuring the domain IO security check (e.g based on JWT/SSO control) when required as API no public capability (e.g ACL based on received event type)
            CapabilityBoundaryAccessControlChecker securityFilteringStep = new CapabilityBoundaryAccessControlChecker(this.domainInputChannel);
            eventTypeFilteringStep.setNext(securityFilteringStep);

            // PROCESSING : identify processor (e.g local capability processor, or remote proxy to dedicated UI capability and/or application processing unit) to activate as responsible to realize the treatment of the event (e.g command interpretation and business rules execution)
            EventProcessingDispatcher processingAssignmentStep = new EventProcessingDispatcher(this.domainInputChannel, this.delegatedExecutionRecipientsAnnouncesStreamConsumer, uisClient, getMessageMapperProvider());
            securityFilteringStep.setNext(processingAssignmentStep);
            pipelinedProcessSingleton = eventTypeFilteringStep;
        }
        return pipelinedProcessSingleton;
    }

    /**
     * Define and execute the pipelined commands according to a responsibility chain pattern.
     * Default entrypoint processing chain executed for each fact event received via the service stream.
     * This implementation is a long-time running process executed into the current thread.
     *
     * @param event To process.
     */
    @Override
    public void notify(IDescribed event) {
        if (event != null) {
            try {
                // Start pipelined processing regarding the event to treat
                pipelinedProcess().handle(event);
            } catch (Exception e) {
                // UnoperationalStateException or IllegalArgumentException thrown by responsibility chain members
                logger.log(Level.SEVERE, e.getMessage());
            }
        }
    }

    @Override
    public IMessageMapperProvider getMessageMapperProvider() {
        return new ACDomainMessageMapperFactory();
    }

    @Override
    public PresenceState currentState() {
        return this.currentPresenceStatus;
    }

    /**
     * Prepare and publish a gateway processing unit command event over the Users Interactions Space allowing to other components to collaborate with the IO pipeline (e.g any Feature module registering routing paths to this DomainIO pipeline for receive future feature command events as routing paths).
     * This method send command into the UICapabilityChannel.access_control_io_gateway_dynamic_routing_plan_evolution channel (that could be observed by any contributor to the routing plan, interested of routing change/request).
     * Potentially could be enhanced in future with DomainIO module presence announce send to API management system monitoring/controlling the domains IO gateway instances.
     *
     * @param presenceState Optional presence current status to announce. When null, this pipeline instance's current status of presence is assigned as default value equals to PresenceState.AVAILABLE.
     * @param priorEventRef Optional origin event that was prior to new event to generate and to publish.
     * @throws Exception When problem during the message preparation of build.
     */
    @Override
    public void announcePresence(PresenceState presenceState, EntityReference priorEventRef) throws Exception {
        if (presenceState == null) {
            // Define the current status of this pipeline which could be announced as available (because it's running instance)
            presenceState = PresenceState.AVAILABLE;
        }

        if (PresenceState.AVAILABLE == presenceState) {
            // Potential additional announce of presence can be published in complement (e.g to gateways management system's control channel)

            // and send request to domain feature modules regarding need of routing plan feeding (registration of their routing paths as managed by this pipeline)
            requestRoutingPlansUpdate(presenceState, priorEventRef);
        } else {
            // Notification end of presence to other components that are interested by the domain IO pipeline status change
            logger.fine(DOMAIN_PIPELINE_LOGICAL_NAME + " pipeline presence end detected without published announce by worker (workerDeploymentId: " + this.deploymentID() + ")");
        }
    }

    private void requestRoutingPlansUpdate(PresenceState presenceState, EntityReference priorEventRef) throws ImmutabilityException, UnoperationalStateException, MappingException {
        // Prepare request command of features routing plans registration

        // Prepare command event's unique identifier
        LinkedHashSet<Identifier> evtUidBasedOn = new LinkedHashSet<>();
        evtUidBasedOn.add(new IdentifierStringBased(BaseConstants.IDENTIFIER_ID.name(),
                /* identifier as performed transaction number */ UUID.randomUUID().toString()));
        if (priorEventRef != null) {
            // Add parent uuid as contributor to the event identification value
            evtUidBasedOn.add(priorEventRef.getEntity().identified());
        }
        DomainEntity commandUID = new DomainEntity(evtUidBasedOn);

        // Prepare event specifications
        Collection<Attribute> definition = new ArrayList<>();
        // Set the logical name of this pipeline which is sender of the event
        definition.add(new Attribute(AttributeName.ServiceName.name(), DYNAMIC_ROUTING_SERVICE_NAME));
        if (presenceState != null) {
            // Set the current presence status
            definition.add(new Attribute(ProcessingUnitPresenceAnnounced.SpecificationAttribute.PRESENCE_STATUS.name(), presenceState.name()));
        }

        // Build command event
        Command requestEvent = CommandFactory.create(/* event type regarding routing plan registration request */CollaborationEventType.PROCESSING_UNIT_PRESENCE_ANNOUNCE_REQUESTED.name(),
                /* Command event identified by */ commandUID, definition, priorEventRef,
                /* None changed domain object */ null);

        // Auto-assign correlation identifier relative to the pipeline deployment identifier
        requestEvent.generateCorrelationId(CorrelationIdFactory.generate(this.deploymentID()));

        // Define the channel normally observed by the domain's feature modules regarding the routing plan changes/demand
        Channel domainIOGateway = new Channel(UICapabilityChannel.access_control_io_gateway_dynamic_routing_plan_evolution.shortName());

        // Publish event to channel
        uisClient.publish(requestEvent, domainIOGateway, new MessageMapperFactory().getMapper(IDescribed.class, String.class));
    }

    /**
     * This implementation make nothing relative to acknowledge interpretation or derived rules.
     *
     * @param presenceDeclarationResultEvent Result of declared presence.
     */
    @Override
    public void manageDeclaredPresenceAcknowledge(IDescribed presenceDeclarationResultEvent) {
        // Do nothing
    }
}
