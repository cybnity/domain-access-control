package org.cybnity.application.accesscontrol.translator.ui.api.event;

import org.cybnity.framework.domain.event.IEventType;

/**
 * Type of API domain event supported by the AC domain.
 * Each event name shall be an adjective representing a fact state.
 * The event types defined here have a "business and capability sens" at the functional level, and are evaluable by external system connected over the User Interface layer.
 */
public enum DomainEventType implements IEventType {

    /**
     * Event about a requested tenant registration in progress.
     */
    TENANT_REGISTRATION_SUBMITTED,

    /**
     * Event about a tenant registration finalized with success.
     */
    TENANT_REGISTERED,

    /**
     * Event about an existing tenant upgraded into the domain layer.
     */
    TENANT_CHANGED,

    /**
     * Event about a tenant deleted from the domain layer.
     */
    TENANT_REMOVED,

    /**
     * Event about refused registration that include a cause (e.g existing tenant already used by other owner).
     */
    TENANT_REGISTRATION_REJECTED;
}
