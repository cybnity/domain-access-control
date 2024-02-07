package org.cybnity.application.accesscontrol.domain.system.gateway.service;

import org.cybnity.application.accesscontrol.translator.ui.api.ACDomainMessageMapperFactory;
import org.cybnity.application.accesscontrol.ui.api.UICapabilityChannel;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.application.vertx.common.event.AttributeName;
import org.cybnity.framework.application.vertx.common.routing.ProcessingUnitAnnouncesObserver;
import org.cybnity.framework.application.vertx.common.service.AbstractEndpointPipelineImpl;
import org.cybnity.framework.application.vertx.common.service.FactBaseHandler;
import org.cybnity.framework.application.vertx.common.service.security.AccessControlChecker;
import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.IDescribed;
import org.cybnity.framework.domain.IdentifierStringBased;
import org.cybnity.framework.domain.event.*;
import org.cybnity.framework.domain.model.DomainEntity;
import org.cybnity.framework.immutable.BaseConstants;
import org.cybnity.framework.immutable.EntityReference;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.framework.immutable.ImmutabilityException;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.*;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.MessageMapperFactory;

import java.util.*;
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
public class DomainIOEventsPipeline extends AbstractEndpointPipelineImpl {

    /**
     * Technical logging
     */
    private static final Logger logger = Logger.getLogger(DomainIOEventsPipeline.class.getName());

    private final ICapabilityChannel pipelineInputChannel = UICapabilityChannel.access_control_in;

    /**
     * UIS entrypoint monitored by this worker.
     */
    private final Stream domainInputChannel = new Stream(pipelineInputChannel.shortName());

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
        super();
    }

    @Override
    protected Logger logger() {
        return logger;
    }

    @Override
    protected void startChannelConsumers() {
        // Create entrypoint of delegates presence announces able to dynamically feed the processing unit recipients list
        delegatedExecutionRecipientsAnnouncesStreamConsumer = new ProcessingUnitAnnouncesObserver(/* Where new routes declaration to manage shall be listened */new Channel(UICapabilityChannel.access_control_pu_presence_announcing.shortName()), featureServiceName(), uisClient,/* Where recipients list changes shall be notified */ new Channel(UICapabilityChannel.access_control_io_gateway_dynamic_routing_plan_evolution.shortName()));
        addTopicConsumer(delegatedExecutionRecipientsAnnouncesStreamConsumer); // Delegate PU announces observer

        try {
            // Register observers on space
            uisClient.subscribe(topicsConsumers, getMessageMapperProvider().getMapper(String.class, IDescribed.class));
            logger.fine(featureModuleLogicalName() + " entrypoint channel consumers started with success by worker (workerDeploymentId: " + this.deploymentID() + ")");
        } catch (UnoperationalStateException e) {
            logger.severe(e.getMessage());
        }
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
        // Name of the consumers group relative to this pipelined entry point stream.
        return NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.PIPELINE, /* domainName */ "ac", /* componentMainFunction */"io",/* resourceType */ null, /* segregationLabel */ "consumers");
    }

    /**
     * Assembly of the events pipeline steps as a singleton responsibility chain and return it.
     *
     * @return A usable stateless pipelined process.
     */
    @Override
    protected FactBaseHandler pipelinedProcess() {
        if (pipelinedProcessSingleton == null) {
            // Build responsibility chain ensuring the command treatment according to the fact conformity

            // FILTER : identify received command as supported by the capability domain
            APISupportedCapabilitySelectionFilter eventTypeFilteringStep = new APISupportedCapabilitySelectionFilter(observed(), this.delegatedExecutionRecipientsAnnouncesStreamConsumer);

            // SECURITY : select optional authenticator ensuring the domain IO security check (e.g based on JWT/SSO control) when required as API no public capability (e.g ACL based on received event type)
            AccessControlChecker securityFilteringStep = new AccessControlChecker(observed(), initSecuredAPICapabilities());
            eventTypeFilteringStep.setNext(securityFilteringStep);

            // PROCESSING : identify processor (e.g local capability processor, or remote proxy to dedicated UI capability and/or application processing unit) to activate as responsible to realize the treatment of the event (e.g command interpretation and business rules execution)
            EventProcessingDispatcher processingAssignmentStep = new EventProcessingDispatcher(observed(), this.delegatedExecutionRecipientsAnnouncesStreamConsumer, uisClient, getMessageMapperProvider());
            securityFilteringStep.setNext(processingAssignmentStep);
            pipelinedProcessSingleton = eventTypeFilteringStep;
        }
        return pipelinedProcessSingleton;
    }

    /**
     * Define the static referential of command or domain event types that require security check.
     * This method defines the referential facts under security check (as equals to secured UI capabilities).
     *
     * @return Collection of event type names.
     */
    private Collection<String> initSecuredAPICapabilities() {
        Collection<String> eventTypeNamesUnderAccessControl = new ArrayList<>();
        // Define authenticator ensuring the domain IO security check (e.g based on JWT/SSO control)
        // like required for secured API capability (e.g ACL based on received event type)
        // For example, event type equals to CommandName.XXXXXXX.name()
        return eventTypeNamesUnderAccessControl;
    }

    @Override
    public IMessageMapperProvider getMessageMapperProvider() {
        return new ACDomainMessageMapperFactory();
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
            // Notification end of presence to other components which are interested in the domain IO pipeline status change
            logger.fine(featureModuleLogicalName() + " pipeline presence end detected without published announce by worker (workerDeploymentId: " + this.deploymentID() + ")");
        }

    }

    @Override
    protected Map<IEventType, ICapabilityChannel> supportedEventTypesToRoutingPath() {
        return null;
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
        definition.add(new Attribute(AttributeName.ServiceName.name(), featureServiceName()));
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
        Channel domainIOGateway = proxyRoutingPlanChangesChannel();

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

    @Override
    public String featureModuleLogicalName() {
        return "AC domain IO";
    }

    @Override
    public String featureServiceName() {
        return NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.GATEWAY, /* domainName */ "ac", /* componentMainFunction */"io",/* resourceType */ null, /* segregationLabel */ null);
    }

    @Override
    public Channel proxyAnnouncingChannel() {
        return null;
    }

    @Override
    public Channel proxyRoutingPlanChangesChannel() {
        return new Channel(UICapabilityChannel.access_control_io_gateway_dynamic_routing_plan_evolution.shortName());
    }
}
