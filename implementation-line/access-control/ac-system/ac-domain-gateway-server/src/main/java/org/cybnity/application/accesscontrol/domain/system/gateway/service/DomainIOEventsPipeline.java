package org.cybnity.application.accesscontrol.domain.system.gateway.service;

import io.lettuce.core.StreamMessage;
import org.cybnity.application.accesscontrol.translator.ui.api.ACDomainMessageMapperFactory;
import org.cybnity.application.accesscontrol.ui.api.UICapabilityChannel;
import org.cybnity.framework.Context;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.application.vertx.common.AbstractMessageConsumerEndpoint;
import org.cybnity.framework.application.vertx.common.routing.ProcessingUnitAnnouncesObserver;
import org.cybnity.framework.application.vertx.common.service.FactBaseHandler;
import org.cybnity.framework.domain.IDescribed;
import org.cybnity.framework.domain.IPresenceObservability;
import org.cybnity.framework.immutable.EntityReference;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.*;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.UISAdapterImpl;

import java.util.ArrayList;
import java.util.Collection;
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
    private final FactBaseHandler pipelinedProcessSingleton;

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

            // Initialize the process pipeline singleton instance
            pipelinedProcessSingleton = pipelinedProcess();
        } catch (IllegalArgumentException iae) {
            // Problem of context read
            throw new UnoperationalStateException(iae);
        }
    }

    /**
     * Enhance the default start process relative to observed channels and/or streams.
     */
    @Override
    public void start() {
        // Execute by default the start operations relative to channels and streams consumers
        super.start();
        // Tag the current operational and active status
        currentPresenceStatus = PresenceState.AVAILABLE;
    }

    /**
     * Enhance the default stop process relative to previously observed channels and/or streams.
     */
    @Override
    public void stop() {
        // Tag the current operational as ended and no active status
        currentPresenceStatus = PresenceState.UNAVAILABLE;

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
        // Register all consumers of observed channels
        uisClient.register(entryPointStreamConsumers, eventMapper);

        logger.fine("AC domain IO entrypoint stream consumers started with success by worker (workerDeploymentId: " + this.deploymentID() + ")");
    }

    @Override
    protected void startChannelConsumers() {
        // Create entrypoint of delegates presence announces able to dynamically feed the processing unit recipients list
        delegatedExecutionRecipientsAnnouncesStreamConsumer = new ProcessingUnitAnnouncesObserver(/* Where new routes declaration to manage shall be listened */new Channel(UICapabilityChannel.access_control_pu_presence_announcing.shortName()), DYNAMIC_ROUTING_SERVICE_NAME, uisClient,/* Where recipients list changes shall be notified */ new Channel(UICapabilityChannel.access_control_io_gateway_dynamic_routing_plan_evolution.shortName()));
        topicsConsumers.add(delegatedExecutionRecipientsAnnouncesStreamConsumer); // Delegate PU announces observer
    }

    @Override
    protected void stopStreamConsumers() {
        // Stop each entrypoint stream previously observed by this worker
        uisClient.unregister(entryPointStreamConsumers);
        // Clean consumers set
        entryPointStreamConsumers.clear();
        logger.fine("AC domain IO entrypoint stream consumers un-registered with success by worker (workerDeploymentId: " + this.deploymentID() + ")");
    }

    @Override
    protected void stopChannelConsumers() {
        // Stop each observed channel by this worker
        uisClient.unsubscribe(topicsConsumers);
        // Clean consumers set
        topicsConsumers.clear();
        logger.fine("AC domain IO gateway consumers unsubscribed with success by worker (workerDeploymentId: " + this.deploymentID() + ")");
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
            return eventTypeFilteringStep;
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
     * Do nothing because currently none system are involved into the dynamic management of the gateway pipelines.
     * Potentially could be enhancement in future to be announced to API management system monitoring/controlling the domains IO gateway instances.
     */
    @Override
    public void announcePresence(PresenceState presenceState, EntityReference entityReference) throws Exception {
        // Do nothing
    }

    /**
     * This implementation make nothing relative to acknowledge interpretation or derived rules.
     * @param presenceDeclarationResultEvent Result of declared presence.
     */
    @Override
    public void manageDeclaredPresenceAcknowledge(IDescribed presenceDeclarationResultEvent) {
        // Do nothing
    }
}
