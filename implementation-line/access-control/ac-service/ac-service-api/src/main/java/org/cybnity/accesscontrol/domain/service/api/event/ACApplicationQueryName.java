package org.cybnity.accesscontrol.domain.service.api.event;

import org.cybnity.framework.domain.event.IEventType;

/**
 * Query command types supported by the Access Control domain.
 * The command types defined here have a "technical sens" at the application level, and are evaluable by integrated components/domains connected over the Application Interface layer.
 */
public enum ACApplicationQueryName implements IEventType {

    /**
     * Name of query allowing to search a tenant data view with equals logical label.
     */
    TENANT_VIEW_FIND_BY_LABEL;

    private ACApplicationQueryName() {
    }
}
