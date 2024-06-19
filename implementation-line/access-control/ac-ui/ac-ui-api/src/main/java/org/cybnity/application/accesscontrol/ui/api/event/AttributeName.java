package org.cybnity.application.accesscontrol.ui.api.event;

/**
 * Type of standard attribute supported by the AC domain events and command.
 * Referential that can be used to identify a type of specification attribute with a value.
 */
public enum AttributeName {

    /**
     * Boolean materializing a state of activity (active, no active).
     */
    ACTIVITY_STATE,

    /**
     * Naming label of a tenant.
     */
    TENANT_LABEL,

    /**
     * Identifier of a Tenant.
     */
    TENANT_ID
}
