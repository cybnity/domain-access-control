package org.cybnity.accesscontrol.domain.service.api.event;

import org.cybnity.framework.domain.event.IEventType;

/**
 * Collection of event types supported by the Access Control domain.
 * The event types defined here have a "technical sens" at the application level, and are evaluable by integrated components/domains connected over the Application Interface layer.
 */
public enum ACApplicationCommandName implements IEventType  {
    /**
     * A tenant domain object have been created.
     */
    TENANT_AGGREGATE_CREATED,
    /**
     * A tenant domain object have be changed.
     */
    TENANT_AGGREGATE_UPDATED,
    /**
     * A tenant optimized data view version have been generated.
     */
    TENANT_VIEW_CREATED,
    /**
     * A tenant optimized data view version have been refreshed.
     */
    TENANT_VIEW_REFRESHED
    ;

    private ACApplicationCommandName() {
    }
}
