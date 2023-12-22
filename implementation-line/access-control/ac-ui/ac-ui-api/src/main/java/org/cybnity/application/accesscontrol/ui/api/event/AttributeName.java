package org.cybnity.application.accesscontrol.ui.api.event;

/**
 * Type of attribute supported by the AC domain events and command.
 * Can be used to identify a type of attribute with a value.
 */
public enum AttributeName {

    /**
     * Attribute relative to the value object equals to a name of organization.
     */
    OrganizationNaming,

    /**
     * Attribute regarding a user's access token (e.g SSO token) usable for access control check.
     */
    AccessToken,

    /**
     * Attribute regarding the logical identification name of a services provider (e.g UI capability processing unit which ensure domain event treatments).
     */
    ServiceName,

    /**
     * Attribute regarding an origin channel name (e.g where a previous origin event was received)
     */
    SourceChannelName;
}
