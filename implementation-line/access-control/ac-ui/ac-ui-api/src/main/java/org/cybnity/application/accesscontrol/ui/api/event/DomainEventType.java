package org.cybnity.application.accesscontrol.ui.api.event;

import org.cybnity.framework.domain.event.IEventType;

/**
 * Type of API domain event supported by the AC domain.
 * Each event name shall be an adjective representing a fact state.
 */
public enum DomainEventType implements IEventType {

    /**
     * Event about a requested organization registration in progress.
     */
    ORGANIZATION_REGISTRATION_SUBMITTED,

    /**
     * Event about an organization registered as valid tenant.
     */
    ORGANIZATION_REGISTERED,

    /**
     * Event about a tenant created by the domain layer.
     */
    TENANT_CREATED,

    /**
     * Event about refused registration that include a cause (e;g existing tenant already used by other owner).
     */
    ORGANIZATION_REGISTRATION_REJECTED;

}
