package org.cybnity.application.accesscontrol.domain.system.gateway.service;

import org.cybnity.application.accesscontrol.domain.system.gateway.AbstractStreamEventRouter;
import org.cybnity.application.accesscontrol.domain.system.gateway.routing.ProcessingUnitAnnouncesObserver;
import org.cybnity.application.accesscontrol.ui.api.UICapabilityChannel;
import org.cybnity.application.accesscontrol.ui.api.experience.ExecutionResource;
import org.cybnity.framework.Context;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.DomainEvent;
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
public class DomainIOEventsPipeline extends AbstractStreamEventRouter implements StreamObserver {

    /**
     * Logical name of the component (reusable in service grid for logical identification when exchanged events between the component and others over the UIS).
     * Optional component name that own the routing service and can be identified in UIS by logical name.
     */
    private static final String DYNAMIC_ROUTING_SERVICE_NAME = "ac" + NamingConventions.SPACE_ACTOR_NAME_SEPARATOR + "io" + NamingConventions.SPACE_ACTOR_NAME_SEPARATOR + ExecutionResource.GATEWAY.name() + NamingConventions.SPACE_ACTOR_NAME_SEPARATOR + "pipeline";

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
     * Collection of fact event consumers (observing DIS entry items) of streams managed by this worker.
     */
    private final Collection<StreamObserver> entryPointStreamConsumers = new ArrayList<>();

    /**
     * Collection of fact event consumers of pub/sub topics listened by this worker.
     */
    private final Collection<ChannelObserver> topicsConsumers = new ArrayList<>();

    /**
     * Listener of processing units' entrypoints that can be used by pipeline as delegates for event treatments.
     */
    private ProcessingUnitAnnouncesObserver delegatedExecutionRecipientsAnnouncesStreamConsumer;

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

    /**
     * Start UIS stream as provided api service entrypoint.
     */
    @Override
    protected void startStreamConsumers() {
        // Create each entrypoint stream observed by this worker
        entryPointStreamConsumers.add(this);// Main IO entrypoint observer

        // Create entrypoint of delegates presence announces able to dynamically feed the processing unit recipients list
        delegatedExecutionRecipientsAnnouncesStreamConsumer = new ProcessingUnitAnnouncesObserver(new Channel(UICapabilityChannel.access_control_pu_presence_announcing.shortName()), DYNAMIC_ROUTING_SERVICE_NAME, uisClient);
        topicsConsumers.add(delegatedExecutionRecipientsAnnouncesStreamConsumer); // Delegate PU announces observer

        // Register all consumers of observed channels
        uisClient.register(entryPointStreamConsumers);

        logger.fine("AC domain IO entrypoint stream consumers started with success by worker (workerDeploymentId: " + this.deploymentID() + ")");
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
    public Stream observed() {
        return domainInputChannel;
    }

    @Override
    public String observationPattern() {
        return null;
    }

    /**
     * Define and execute the pipelined commands according to a responsibility chain pattern.
     *
     * @param domainEvent To process.
     */
    @Override
    public void notify(DomainEvent domainEvent) {
        logger.log(Level.SEVERE, "Received domain event is supported by the AC domain IO entrypoint!");
    }

    /**
     * Define and execute the pipelined commands according to a responsibility chain pattern.
     * Default entrypoint processing chain executed for each fact event received via the service stream.
     * This implementation is a long-time running process executed into the current thread.
     *
     * @param command To process.
     */
    @Override
    public void notify(Command command) {
        if (command != null) {
            try {
                // Build responsibility chain ensuring the command treatment according to the fact conformity

                // FILTER : identify received command as supported by the capability domain
                APISupportedCapabilitySelectionFilter eventTypeFilteringStep = new APISupportedCapabilitySelectionFilter(this.domainInputChannel);

                // FILTER : select optional authenticator ensuring the domain IO security check (e.g based on JWT/SSO control) when required as API no public capability (e.g ACL based on received event type)
                CapabilityBoundaryAccessControlChecker securityFilteringStep = new CapabilityBoundaryAccessControlChecker(this.domainInputChannel);
                eventTypeFilteringStep.setNext(securityFilteringStep);

                // PROCESSING : identify processor (e.g local capability processor, or remote proxy to dedicated UI capability and/or application processing unit) to activate as responsible to realize the treatment of the event (e.g command interpretation and business rules execution)
                EventProcessingDispatcher processingAssignmentStep = new EventProcessingDispatcher(this.domainInputChannel, this.delegatedExecutionRecipientsAnnouncesStreamConsumer, uisClient);
                securityFilteringStep.setNext(processingAssignmentStep);

                // Start pipelined processing from first step
                eventTypeFilteringStep.handle(command);
            } catch (Exception e) {
                // UnoperationalStateException or IllegalArgumentException thrown by responsibility chain members
                logger.log(Level.SEVERE, e.getMessage());
            }
        }
    }
}
