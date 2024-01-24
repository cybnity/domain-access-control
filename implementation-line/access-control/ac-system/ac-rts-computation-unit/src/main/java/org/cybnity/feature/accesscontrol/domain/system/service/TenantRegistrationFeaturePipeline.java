package org.cybnity.feature.accesscontrol.domain.system.service;

import io.lettuce.core.StreamMessage;
import org.cybnity.application.accesscontrol.translator.ui.api.ACDomainMessageMapperFactory;
import org.cybnity.application.accesscontrol.ui.api.UICapabilityChannel;
import org.cybnity.application.accesscontrol.ui.api.event.CommandName;
import org.cybnity.application.accesscontrol.ui.api.experience.ExecutionResource;
import org.cybnity.framework.Context;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.application.vertx.common.AbstractMessageConsumerEndpoint;
import org.cybnity.framework.application.vertx.common.routing.DomainIOGatewayRecipientsManagerObserver;
import org.cybnity.framework.application.vertx.common.service.FactBaseHandler;
import org.cybnity.framework.domain.IDescribed;
import org.cybnity.framework.domain.IPresenceObservability;
import org.cybnity.framework.domain.event.IEventType;
import org.cybnity.framework.domain.event.ProcessingUnitPresenceAnnounced;
import org.cybnity.framework.immutable.EntityReference;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.*;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.event.ProcessingUnitPresenceAnnouncedEventFactory;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.UISAdapterImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Feature ensuring the registration of a Tenant into the Access Control domain according to use case scenario and prerequisite conditions.
 * This use case realization consumes registration command event from UI layer, validate mandatory data, and implement the scenario specified by the referenced requirement documentation.
 */
public class TenantRegistrationFeaturePipeline extends AbstractMessageConsumerEndpoint implements StreamObserver, IPresenceObservability {

    /**
     * Technical logging
     */
    private static final Logger logger = Logger.getLogger(TenantRegistrationFeaturePipeline.class.getName());

    /**
     * Functional status based on the operational state of all consumers started and active.
     */
    private PresenceState currentPresenceStatus;

    /**
     * Client managing interactions with Users Interactions Space.
     */
    private final UISAdapter uisClient;

    /**
     * Collection of fact event consumers (observing DIS entry items) of streams managed by this worker.
     */
    private final Collection<StreamObserver> entryPointStreamConsumers = new ArrayList<>();

    private final ICapabilityChannel pipelineInputChannel = UICapabilityChannel.access_control_tenant_registration;

    /**
     * UIS entrypoint monitored by this worker.
     */
    private final Stream featureInputChannel = new Stream(pipelineInputChannel.shortName());

    /**
     * Logical name of the component (reusable in service grid for logical identification when exchanged events between the component and others over the UIS).
     * Optional component name that own the routing service and can be identified in UIS by logical name.
     */
    private static final String FEATURE_SERVICE_NAME = NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.PIPELINE, /* domainName */ "ac", /* componentMainFunction */"tenant_registration",/* resourceType */ ExecutionResource.PROCESSING_UNIT.label(), /* segregationLabel */ null);

    /**
     * Name of the consumers group relative to this pipelined entry point stream.
     */
    private static final String CONSUMERS_GROUP_NAME = NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.FEATURE_PROCESSING_UNIT, /* domainName */ "ac", /* componentMainFunction */"tenant-registration",/* resourceType */ null, /* segregationLabel */ "consumers");

    /**
     * Collection of fact event consumers of pub/sub topics listened by this worker.
     */
    private final Collection<ChannelObserver> topicsConsumers = new ArrayList<>();

    /**
     * Entrypoint events treatment pipeline singleton.
     */
    private FactBaseHandler pipelinedProcessSingleton;

    /**
     * Default constructor.
     *
     * @throws UnoperationalStateException When problem of context configuration (e.g missing environment variable defined to join the UIS or DIS).
     */
    public TenantRegistrationFeaturePipeline() throws UnoperationalStateException {
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

    /**
     * Enhance the default start process relative to observed channels and/or streams, with the send of event announcing the presence of this processing unit to other systems (e.g domain IO Gateway).
     */
    @Override
    public void start() {
        // Execute by default the start operations relative to channels and streams consumers
        super.start();
        // Tag the current operational and active status
        currentPresenceStatus = PresenceState.AVAILABLE;

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
     * Enhance the default stop process relative to previously observed channels and/or streams, with the send of event notifying the end of processing unit presence (e.g to domain IO Gateway that should stop to forward feature execution requests to this component).
     */
    @Override
    public void stop() {
        // Tag the current operational as ended and no active status
        currentPresenceStatus = PresenceState.UNAVAILABLE;

        // Execute by default the stop operations relative to channels and streams previously observed
        super.stop();
    }

    /**
     * Start the listening of the entrypoint channel allowing to treat the feature realization requests.
     */
    @Override
    protected void startStreamConsumers() {
        // Create each entrypoint stream observed by this worker
        entryPointStreamConsumers.add(this);// Feature entrypoint observer

        // Define usable mapper supporting the read of stream message received from the Users Interactions Space and translated into domain event types
        MessageMapper eventMapper = getMessageMapperProvider().getMapper(StreamMessage.class, IDescribed.class);
        // Register all consumers of observed channels
        uisClient.register(entryPointStreamConsumers, eventMapper);

        logger.fine("AC domain TenantRegistration entrypoint stream consumers started with success by worker (workerDeploymentId: " + this.deploymentID() + ")");
    }

    /**
     * Subscribe to observed channels, and send announce to domain IO Gateway as feature presence start allowing to dynamically register routing path of event type supported by this feature implementation component.
     */
    @Override
    protected void startChannelConsumers() {
        // Listen the confirmation of routes registered by the domain gateway (as CollaborationEventType.PROCESSING_UNIT_ROUTING_PATHS_REGISTERED.name() event)
        // From the gateway's managed topic
        // Listening of acknowledges (announced presence confirmed registration) able to dynamically manage the eventual need retry to perform about the feature unit registration into the domain IO Gateway's recipients list
        DomainIOGatewayRecipientsManagerObserver recipientsManagerOutputsObserver = new DomainIOGatewayRecipientsManagerObserver(new Channel(UICapabilityChannel.access_control_io_gateway_dynamic_routing_plan_evolution.shortName()), this);
        topicsConsumers.add(recipientsManagerOutputsObserver);
        // Register observers on space
        uisClient.subscribe(topicsConsumers, getMessageMapperProvider().getMapper(String.class, IDescribed.class));
    }

    @Override
    protected void stopStreamConsumers() {
        // Stop each entrypoint stream previously observed by this worker
        uisClient.unregister(entryPointStreamConsumers);
        // Clean consumers set
        entryPointStreamConsumers.clear();
        logger.fine("AC domain TenantRegistration entrypoint stream consumers un-registered with success by worker (workerDeploymentId: " + this.deploymentID() + ")");
    }

    /**
     * Unsubscribe to topics, and send announce to domain IO Gateway as feature presence end.
     */
    @Override
    protected void stopChannelConsumers() {
        // Stop each observed channel by this worker
        uisClient.unsubscribe(topicsConsumers);
        // Clean consumers set
        topicsConsumers.clear();
        logger.fine("AC domain TenantRegistration consumers unsubscribed with success by worker (workerDeploymentId: " + this.deploymentID() + ")");
    }


    /**
     * Assembly of the events pipeline steps as a singleton responsibility chain and return it.
     *
     * @return A usable stateless pipelined process.
     */
    private FactBaseHandler pipelinedProcess() {
        if (pipelinedProcessSingleton == null) {
            // TODO coder le processus métier pipeliné répondant aux scénarios de supportés par la feature/exigences
            // Build responsibility chain ensuring the command treatment according to the fact conformity

            // FILTER : identify received command as supported by the capability domain
            TenantRegistrationProcessor filteringStep = new TenantRegistrationProcessor();

            // PROCESSING : ...
            // TODO coder le traitement des events entrant depuis le endpoint
            //EventProcessingDispatcher processingAssignmentStep = new EventProcessingDispatcher(this.domainInputChannel, this.delegatedExecutionRecipientsAnnouncesStreamConsumer, uisClient, getMessageMapperProvider());
            //securityFilteringStep.setNext(processingAssignmentStep);
            pipelinedProcessSingleton = filteringStep;
        }
        return pipelinedProcessSingleton;
    }

    @Override
    public IMessageMapperProvider getMessageMapperProvider() {
        return new ACDomainMessageMapperFactory();
    }

    @Override
    public Stream observed() {
        return featureInputChannel;
    }

    /**
     * Collect and treat all the event received from the entrypoint stream.
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
     * Read the current operational presence status.
     *
     * @return Available state when all consumers are in operational status. Else return PresenceState.UNAVAILABLE status.
     */
    @Override
    public PresenceState currentState() {
        return currentPresenceStatus;
    }

    /**
     * Prepare and publish a feature processing unit presence event over the Users Interactions Space allowing to other component to collaborate with the feature (e.g Gateway forwarding command events as request of tenant registration).
     * This method announces the supported entry point event types (CommandName.REGISTER_ORGANIZATION).
     *
     * @param presenceState Presence current status to announce. When null, this pipeline instance's current status of presence is assigned as default value equals to PresenceState.AVAILABLE.
     * @param priorEventRef Optional origin event (e.g request of announce renewal received from domain IO Gateway) that was prior to new event to generate and to publish.
     * @throws Exception When problem during the message preparation of build.
     */
    @Override
    public void announcePresence(PresenceState presenceState, EntityReference priorEventRef) throws Exception {
        if (presenceState == null) {
            // Define the current status of this pipeline which could be announced as available (because it's running instance)
            presenceState = PresenceState.AVAILABLE;
        }

        Map<IEventType, ICapabilityChannel> supportedEventTypesToRoutingPath = new HashMap<>();
        // Define event type supported by this feature pipeline, and observed from an entrypoint
        supportedEventTypesToRoutingPath.put(CommandName.REGISTER_ORGANIZATION, pipelineInputChannel);

        // Prepare event to presence announcing channel
        ProcessingUnitPresenceAnnounced presenceAnnounce = new ProcessingUnitPresenceAnnouncedEventFactory().create(supportedEventTypesToRoutingPath, FEATURE_SERVICE_NAME, priorEventRef, presenceState);
        Channel domainIOGateway = new Channel(UICapabilityChannel.access_control_pu_presence_announcing.shortName());
        // Publish event to channel
        uisClient.publish(presenceAnnounce, domainIOGateway, getMessageMapperProvider().getMapper(presenceAnnounce.getClass(), String.class));
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

    /**
     * Define and execute the pipelined commands according to a responsibility chain pattern.
     * Default entrypoint processing chain executed for each fact event received via the feature stream.
     * This implementation is a long-time running process executed into the current thread.
     *
     * @param event To process.
     */
    @Override
    public void notify(IDescribed event) {
        try {
            // Execute the feature execution process/pipeline according to the received event type (e.g CommandName.REGISTER_ORGANIZATION)
            pipelinedProcess().handle(event);
        } catch (Exception e) {
            // UnoperationalStateException or IllegalArgumentException thrown by responsibility chain members
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

}
