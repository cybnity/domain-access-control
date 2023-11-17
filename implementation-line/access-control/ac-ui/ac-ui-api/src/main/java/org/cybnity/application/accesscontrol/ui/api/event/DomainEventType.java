package org.cybnity.application.accesscontrol.ui.api.event;

/**
 * Type of API domain event supported by the AC domain.
 */
public enum DomainEventType {

    /**
     * Event about an organization registered as eligible to become a tenant.
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
