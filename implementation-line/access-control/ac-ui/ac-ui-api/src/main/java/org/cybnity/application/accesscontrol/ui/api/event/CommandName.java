package org.cybnity.application.accesscontrol.ui.api.event;

/**
 * Type of API command event supported by the AC domain.
 * Each command name shall be a verb.
 */
public enum CommandName {

    /**
     * Registration command of an organization eligible to become a tenant as perimeter of access control.
     */
    REGISTER_ORGANIZATION,

    /**
     * Registration command of a user account for an existing organization.
     */
    REGISTER_ACCOUNT,

    /**
     * Activate an existing pre-registered account that become usable by a person into a tenant perimeter.
     */
    ACTIVATE_REGISTERED_ACCOUNT;
}
