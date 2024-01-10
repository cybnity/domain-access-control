package org.cybnity.feature.accesscontrol.domain.system.service;

import io.lettuce.core.StreamMessage;
import org.cybnity.application.accesscontrol.translator.ui.api.ACDomainMessageMapperFactory;
import org.cybnity.application.accesscontrol.ui.api.UICapabilityChannel;
import org.cybnity.framework.Context;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.application.vertx.common.AbstractMessageConsumerEndpoint;
import org.cybnity.framework.domain.IDescribed;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.*;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.UISAdapterImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Feature ensuring the registration of a Tenant into the Access Control domain according to use case scenario and prerequisite conditions.
 * This use case realization consumes registration command event from UI layer, validate mandatory data, and implement the scenario specified by the referenced requirement documentation.
 *
 */
public class TenantRegistrationFeaturePipeline extends AbstractMessageConsumerEndpoint implements StreamObserver {

    /**
     * Technical logging
     */
    private static final Logger logger = Logger.getLogger(TenantRegistrationFeaturePipeline.class.getName());

    /**
     * Client managing interactions with Users Interactions Space.
     */
    private final UISAdapter uisClient;

    /**
     * Collection of fact event consumers (observing DIS entry items) of streams managed by this worker.
     */
    private final Collection<StreamObserver> entryPointStreamConsumers = new ArrayList<>();

    /**
     * UIS entrypoint monitored by this worker.
     */
    private final Stream featureInputChannel = new Stream(UICapabilityChannel.access_control_tenant_registration.shortName());

    /**
     * Name of the consumers group relative to this pipelined entry point stream.
     */
    private static final String CONSUMERS_GROUP_NAME = NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.FEATURE_PROCESSING_UNIT, /* domainName */ "ac", /* componentMainFunction */"tenant-registration",/* resourceType */ null, /* segregationLabel */ "consumers");

    /**
     * Collection of fact event consumers of pub/sub topics listened by this worker.
     */
    private final Collection<ChannelObserver> topicsConsumers = new ArrayList<>();

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
     * Prepare and publish a feature processing unit presence event over the Users Interactions Space allowing to other component to collaborate with the feature (e.g Gateway forwarding command events as request of tenant registration)
     */
    private void announceFeaturePresence() {
        // TODO ajouter l'envoi de l'announced par usage de ProcessingUnitPresenceAnnouncedEventFactory
        // en indiquant quel type d'event supporté, quel point d'endpoint etc...
    }

    /**
     * Prepare and publish a presence end event over the UIS.
     */
    private void announceFeaturePresenceEnd() {
        // TODO envoyer un event de fin de presence permettant à la gateway de supprimer les typesd d'event et channel n'etant plus observé par cette feature

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

        // Promote announce about the supported event type consumed from the channel type
        announceFeaturePresence();
    }

    @Override
    protected void startChannelConsumers() {
        // TODO ecouter les announced confirmé par la gateway pour traiter l'éventuel cas de retry
        // Create entrypoint of announced presence confirmed registration able to dynamically manage the potential retry to perform about the feature unit registration into the domain IO Gateway's recipients list
        //delegatedExecutionRecipientsAnnouncesStreamConsumer = new ProcessingUnitAnnouncesObserver(new Channel(UICapabilityChannel.access_control_pu_presence_announcing.shortName()), DYNAMIC_ROUTING_SERVICE_NAME, uisClient);
        //topicsConsumers.add(delegatedExecutionRecipientsAnnouncesStreamConsumer); // Delegate Gateway announces observer

        // Create the eventual restart of the AC Domain IO Gateway that need to be again notified of this feature entrypoint events/route
        // TODO créer observer de restart announced par la IO gateway et relancer un event d'annonce des routes vers ce entrypoint


        // Promote announce about the supported event type consumed from the channel type
        announceFeaturePresence();
    }

    @Override
    protected void stopStreamConsumers() {
        // Stop each entrypoint stream previously observed by this worker
        uisClient.unregister(entryPointStreamConsumers);
        // Clean consumers set
        entryPointStreamConsumers.clear();
        logger.fine("AC domain TenantRegistration entrypoint stream consumers un-registered with success by worker (workerDeploymentId: " + this.deploymentID() + ")");

        // Notify end of presence regarding the channel type
        announceFeaturePresenceEnd();
    }

    @Override
    protected void stopChannelConsumers() {
        // Stop each observed channel by this worker
        uisClient.unsubscribe(topicsConsumers);
        // Clean consumers set
        topicsConsumers.clear();
        logger.fine("AC domain TenantRegistration consumers unsubscribed with success by worker (workerDeploymentId: " + this.deploymentID() + ")");
        // Notify end of presence regarding the channel type
        announceFeaturePresenceEnd();
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

    @Override
    public void notify(IDescribed iDescribed) {
        // TODO coder le traitement des events entrant depuis le endpoint
    }
}
