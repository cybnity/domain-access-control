package org.cybnity.application.accesscontrol.domain.system.gateway.service;

import org.cybnity.application.accesscontrol.domain.system.gateway.AbstractStreamEventRouter;
import org.cybnity.application.accesscontrol.ui.api.UICapabilityChannel;
import org.cybnity.framework.Context;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.DomainEvent;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.Channel;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.StreamObserver;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.UISAdapter;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.UISAdapterImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Public API service managing the events supported by a domain.
 * Use the Pipes and Filters architectural style to divide a larger processing task into a sequence of smaller, independent processing steps (Filters) that are connected by channels (Pipes).
 * Each filter exposes a very simple interface: it receives events on the inbound pipe, processes the message (e.g identify if supported and can be processed by a public or secure performer), and publishes the results to the outbound pipe (e.g delegation to a dedicated capability processor).
 * The pipe connects one filter to the next, sending output messages from one filter to the next.
 * Because all component use the same external interface they can be composed into different solutions by connecting the components to different pipes.
 * We can add new filters, omit existing ones or rearrange them into a new sequence -- all without having to change the filters themselves. The connection between filter and pipe is sometimes called port. In the basic form, each filter component has one input port and one output port.
 */
public class DomainIOEventsPipeline extends AbstractStreamEventRouter implements StreamObserver {

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
    private final Channel domainInputChannel = new Channel(UICapabilityChannel.access_control_in.shortName());

    /**
     * Collection of fact event consumers (observing DIS entry items) managed by this worker.
     */
    private final Collection<StreamObserver> entryPointStreamConsumers = new ArrayList<>();

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
    public Channel observed() {
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
                EventProcessingDispatcher processingAssignmentStep = new EventProcessingDispatcher(this.domainInputChannel);
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
