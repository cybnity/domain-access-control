package org.cybnity.application.accesscontrol.ui.api.event;

/**
 * Type of attribute supported by the AC domain events and command regarding tenant registration capability realization.
 * Referential that can be used to identify a type of specification attribute with a value.
 */
public enum TenantRegistrationAttributeName {

    /**
     * Attribute relative to the value object equals to a name of organization.
     */
    ORGANIZATION_NAMING,

    /**
     * Quantity of active accounts.
     */
    ACTIVE_ACCOUNTS_COUNT;
}
