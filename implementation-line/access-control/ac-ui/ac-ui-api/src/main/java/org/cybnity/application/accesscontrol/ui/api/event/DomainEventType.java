package org.cybnity.application.accesscontrol.ui.api.event;

/**
 * Type of API domain event supported by the AC domain.
 * Each event name shall be an adjective representing a fact state.
 */
public enum DomainEventType {

    /**
     * Event about a requested organization registration
     */
    ORGANIZATION_REGISTRATION_SUBMITTED,

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
    ORGANIZATION_REGISTRATION_REJECTED,

    /**
     * Event about a user account registered for usage into an organization perimeter.
     */
    ACCOUNT_REGISTERED,

    /**
     * Event about a user account that is active and is usable by its owner.
     */
    ACCOUNT_ACTIVATED,

    ;
}
