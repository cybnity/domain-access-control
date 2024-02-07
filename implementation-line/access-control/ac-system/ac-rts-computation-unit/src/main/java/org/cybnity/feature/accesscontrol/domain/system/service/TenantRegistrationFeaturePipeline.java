package org.cybnity.feature.accesscontrol.domain.system.service;

import org.cybnity.application.accesscontrol.translator.ui.api.ACDomainMessageMapperFactory;
import org.cybnity.application.accesscontrol.ui.api.UICapabilityChannel;
import org.cybnity.application.accesscontrol.ui.api.event.CommandName;
import org.cybnity.application.accesscontrol.ui.api.experience.ExecutionResource;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.application.vertx.common.service.AbstractEndpointPipelineImpl;
import org.cybnity.framework.application.vertx.common.service.FactBaseHandler;
import org.cybnity.framework.application.vertx.common.service.filter.InterestEventFilter;
import org.cybnity.framework.application.vertx.common.service.security.AccessControlChecker;
import org.cybnity.framework.domain.IDescribed;
import org.cybnity.framework.domain.event.IEventType;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Feature ensuring the registration of a Tenant into the Access Control domain according to use case scenario and prerequisite conditions.
 * This use case realization consumes registration command event from UI layer, validate mandatory data, and implement the scenario specified by the referenced requirement documentation.
 */
public class TenantRegistrationFeaturePipeline extends AbstractEndpointPipelineImpl {

    /**
     * Technical logging
     */
    private static final Logger logger = Logger.getLogger(TenantRegistrationFeaturePipeline.class.getName());

    private final ICapabilityChannel pipelineInputChannel = UICapabilityChannel.access_control_tenant_registration;

    /**
     * UIS entrypoint monitored by this worker.
     */
    private final Stream featureInputChannel = new Stream(pipelineInputChannel.shortName());

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
        super();
    }

    @Override
    protected Logger logger() {
        return logger;
    }

    @Override
    public String featureModuleLogicalName() {
        return "AC domain TenantRegistration";
    }

    @Override
    public String featureServiceName() {
        return NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.PIPELINE, /* domainName */ "ac", /* componentMainFunction */"tenant_registration",/* resourceType */ ExecutionResource.PROCESSING_UNIT.label(), /* segregationLabel */ null);
    }

    @Override
    protected FactBaseHandler pipelinedProcess() {
        if (pipelinedProcessSingleton == null) {
            // Build responsibility chain ensuring the command treatment according to the fact conformity

            // FILTER : identify received command as supported by the feature
            InterestEventFilter eventTypeFilteringStep = new InterestEventFilter(supportedEventTypesToRoutingPath().keySet());

            // SECURITY : select optional authenticator ensuring the domain IO security check (e.g based on JWT/SSO control) when required as API no public capability (e.g ACL based on received event type)
            AccessControlChecker securityFilteringStep = new AccessControlChecker(observed(), initSecuredFunctions());
            eventTypeFilteringStep.setNext(securityFilteringStep);

            // PROCESSING : ...
            // TODO coder le traitement des events entrant depuis le endpoint selon on pattern de splitter selon le type de message ou selective consumer
            //EventProcessingDispatcher processingAssignmentStep = new EventProcessingDispatcher(this.domainInputChannel, this.delegatedExecutionRecipientsAnnouncesStreamConsumer, uisClient, getMessageMapperProvider());
            //securityFilteringStep.setNext(processingAssignmentStep);
            pipelinedProcessSingleton = eventTypeFilteringStep;
        }
        return pipelinedProcessSingleton;
    }


    /**
     * Define the static referential of command or domain event types that require security check.
     * This method defines the referential facts under security check (as equals to secured functions).
     *
     * @return Collection of event type names.
     */
    private Collection<String> initSecuredFunctions() {
        Collection<String> eventTypeNamesUnderAccessControl = new ArrayList<>();
        // Define authenticator ensuring the pipeline security check (e.g based on JWT/SSO control)
        // like required for secured function (e.g ACL based on received event type)
        // For example, event type equals to CommandName.XXXXXXX.name()
        return eventTypeNamesUnderAccessControl;
    }

    @Override
    protected Map<IEventType, ICapabilityChannel> supportedEventTypesToRoutingPath() {
        Map<IEventType, ICapabilityChannel> supportedEventTypesToRoutingPath = new HashMap<>();

        // --- Define each event type supported by this feature pipeline, and observed from an entrypoint ---
        supportedEventTypesToRoutingPath.put(CommandName.REGISTER_ORGANIZATION, pipelineInputChannel);

        return supportedEventTypesToRoutingPath;
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
        // Name of the consumers group relative to this pipelined entry point stream.
        return NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.FEATURE_PROCESSING_UNIT, /* domainName */ "ac", /* componentMainFunction */"tenant-registration",/* resourceType */ null, /* segregationLabel */ "consumers");
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
    public Channel proxyAnnouncingChannel() {
        return new Channel(UICapabilityChannel.access_control_pu_presence_announcing.shortName());
    }

    @Override
    public Channel proxyRoutingPlanChangesChannel() {
        return new Channel(UICapabilityChannel.access_control_io_gateway_dynamic_routing_plan_evolution.shortName());
    }

}
