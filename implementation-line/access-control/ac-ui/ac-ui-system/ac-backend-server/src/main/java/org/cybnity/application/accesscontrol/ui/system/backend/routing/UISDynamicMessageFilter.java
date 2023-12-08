package org.cybnity.application.accesscontrol.ui.system.backend.routing;

import org.cybnity.application.accesscontrol.ui.api.UICapabilityChannel;
import org.cybnity.application.accesscontrol.ui.api.event.CommandName;

import java.util.HashMap;
import java.util.Map;

/**
 * Message filter allowing to identify some fact event bus types names, with the names of
 * the UI domain capability entry point (e.g redis stream channel ensuring main entrypoint of a capability domain) allowing to bridge the
 * events forwarding. This implementation helps to implement the Content-Based
 * Router pattern where the recipient channel is identified from the message
 * content to forward. The goal of the recipients list (e.g defining domain UI capability entrypoint channel for type of message) used by this message filter allowed by
 * this mapper is to manage (on server-side and hidden from the client side
 * source code's url and/or javascript) the routing in a generic way based on
 * the capabilities api supporting the event naming between the event bus
 * and the Redis space.
 * <p>
 * It's an implementation of architectural pattern named "Message Filter".
 */
public class UISDynamicMessageFilter {

    /**
     * Referential of routes dedicated to specific event types which can be managed by Users Interactions Space's channels.
     * Each item is defined by an event type name (key) and a UIS recipient path (value identifying a topic/stream name).
     */
    private Map<String, Enum<?>> routingMap;

    /**
     * Default constructor initializing the routing table. This configuration
     * implementation example of linked event types could be replaced by a routing
     * configuration file that is easy to maintain (e.g with possible hot change
     * supported) with settings hosted by the domains-interactions-broker module (e.g
     * as configuration api).
     */
    public UISDynamicMessageFilter() {
        // Initialize the routing destination tables that link an event bus channel with
        // a redis channel
        routingMap = new HashMap<>();

        // Set each domain destination path supporting each type of authorized message
        routingMap.put(CommandName.REGISTER_ORGANIZATION.name(), UICapabilityChannel.access_control_in);// Global entrypoint supporting organization registration command
    }

    /**
     * Find a route to Users Interactions Space's channel supporting a type of event.
     *
     * @param aFactEventTypeName Name of fact event type which shall be supported by a channel as routing path to find.
     * @return A recipient channel or null.
     */
    public UICapabilityChannel recipient(String aFactEventTypeName) {
        if (aFactEventTypeName != null && !aFactEventTypeName.isEmpty()) {
            // Find existing UIS channel routing
            return (UICapabilityChannel) routingMap.get(aFactEventTypeName);
        }
        return null;
    }
}
