package org.cybnity.application.accesscontrol.ui.system.backend.routing;

import org.cybnity.application.accesscontrol.ui.api.UICapabilityChannel;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class allowing to map the event bus channels names, with the names of
 * the UI capability entry points (redis stream channels) allowing to bridge the
 * events forwarding. This implementation helps to implement the Content-Based
 * Router pattern where the recipient channel is identified from the message
 * content to forward. The goal of the recipients list identification allowed by
 * this mapper is to manage (on server-side and hidden from the client side
 * source code's url and/or javascript) the routing in a generic way based on
 * the capabilities' names supporting the channels naming between the event bus
 * and the redis space.
 */
public class UISDynamicDestinationList {

    private Map<String, Enum<?>> routingMap;

    /**
     * Default constructor initializing the routing table. This configuration
     * implementation example of linked channels could be replaced by a routing
     * configuration file that is easy to maintain (e.g with possible hot change
     * supported) with settings hosted by the domains-interactions-broker module (e.g
     * as configuration api).
     */
    public UISDynamicDestinationList() {
        // Initialize the routing destination tables that link a event bus channel with
        // a redis channel
        routingMap = new HashMap<String, Enum<?>>();

        // Set each destination path
        routingMap.put(CollaborationChannel.ac_in.label(), UICapabilityChannel.access_control);
    }

    /**
     * Find a route to Users Interactions Space's channel.
     *
     * @param aCollaborationChannelLabel Origin channel which is subject of possible
     *                                   routing to find.
     * @return A recipient channel or null.
     */
    public Enum<?> recipient(String aCollaborationChannelLabel) {
        if (aCollaborationChannelLabel != null && !aCollaborationChannelLabel.isEmpty()) {
            // Find existing UIS channel routing
            return routingMap.get(aCollaborationChannelLabel);
        }
        return null;
    }
}
