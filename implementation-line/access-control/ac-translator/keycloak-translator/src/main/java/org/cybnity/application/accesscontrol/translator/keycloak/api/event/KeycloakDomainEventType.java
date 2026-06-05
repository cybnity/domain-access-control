package org.cybnity.application.accesscontrol.translator.keycloak.api.event;

import org.cybnity.framework.domain.event.IEventType;

/**
 * Type of Keycloak API domain event supported by the AC domain.
 * Each event name shall be an adjective representing a fact state relative to Keycloak domain.
 * The event types defined here have a "business and capability sens" at the functional level, and are evaluable by external system connected over the Keycloak API.
 */
public enum KeycloakDomainEventType implements IEventType {

    /**
     * Event about a requested realm registration in progress.
     */
    REALM_REGISTRATION_SUBMITTED,

    /**
     * Event about a realm registration finalized with success.
     */
    REALM_REGISTERED,

    /**
     * Event about an existing realm upgraded into the domain layer.
     */
    REALM_CHANGED,

    /**
     * Event about a realm deleted from the domain layer.
     */
    REALM_REMOVED,

    /**
     * Event about refused registration that include a cause (e.g existing duplicate realm).
     */
    REALM_REGISTRATION_REJECTED;
}
