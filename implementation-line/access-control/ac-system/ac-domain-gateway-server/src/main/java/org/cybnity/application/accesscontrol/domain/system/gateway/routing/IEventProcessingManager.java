package org.cybnity.application.accesscontrol.domain.system.gateway.routing;

import org.cybnity.application.accesscontrol.ui.api.routing.UISRecipientList;

/**
 * Processing management contract regarding one or several event types.
 */
public interface IEventProcessingManager {

    /**
     * Get the map of supported event types and processing supporters.
     *
     * @return A map.
     */
    public UISRecipientList delegateDestinations();
}
