package org.cybnity.feature.accesscontrol.domain.system.service;

import org.cybnity.framework.application.vertx.common.AbstractMessageConsumerEndpoint;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.IMessageMapperProvider;

/**
 * Feature ensuring the registration of a Tenant into the Access Control domain according to use case scenario and prerequisite conditions.
 * This use case realization consumes registration command event from UI layer, validate mandatory data, and implement the scenario specified by the referenced requirement documentation.
 *
 */
public class TenantRegistrationFeaturePipeline extends AbstractMessageConsumerEndpoint {

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

    @Override
    protected void startStreamConsumers() {

        // Promote announce about the supported event type consumed from the channel type
        announceFeaturePresence();
    }

    @Override
    protected void startChannelConsumers() {

        // Promote announce about the supported event type consumed from the channel type
        announceFeaturePresence();
    }

    @Override
    protected void stopStreamConsumers() {

        // Notify end of presence regarding the channel type
        announceFeaturePresenceEnd();
    }

    @Override
    protected void stopChannelConsumers() {

        // Notify end of presence regarding the channel type
        announceFeaturePresenceEnd();
    }

    @Override
    public IMessageMapperProvider getMessageMapperProvider() {
        return null;
    }
}
