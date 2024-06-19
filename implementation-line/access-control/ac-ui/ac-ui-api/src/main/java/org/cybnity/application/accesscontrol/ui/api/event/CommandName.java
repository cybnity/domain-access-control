package org.cybnity.application.accesscontrol.ui.api.event;

import org.cybnity.framework.domain.event.IEventType;

/**
 * Type of API command event supported by the AC domain.
 * Each command name shall be a verb.
 */
public enum CommandName implements IEventType {

    /**
     * Registration command of a tenant eligible to become a perimeter of access control.
     */
    REGISTER_TENANT,

    /**
     * Upgrade an existing tenant.
     */
    UPGRADE_TENANT,

    /**
     * Registration command of a user account for an existing tenant.
     */
    REGISTER_ACCOUNT,

    /**
     * Activate an existing pre-registered account that become usable by a person into a tenant perimeter.
     */
    ACTIVATE_REGISTERED_ACCOUNT
}
