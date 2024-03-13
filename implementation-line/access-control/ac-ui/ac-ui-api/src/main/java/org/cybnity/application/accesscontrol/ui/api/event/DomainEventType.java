package org.cybnity.application.accesscontrol.ui.api.event;

import org.cybnity.framework.domain.event.IEventType;

/**
 * Type of API domain event supported by the AC domain.
 * Each event name shall be an adjective representing a fact state.
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
