package org.cybnity.application.accesscontrol.domain.system.gateway.routing;

import org.cybnity.application.accesscontrol.ui.api.UICapabilityChannel;

import java.util.HashMap;
import java.util.Map;

/**
 * Identify the correct recipient based on an event's content.
 * A recipient channel is defined for each event type supported by the capability domain.
 * Identify each stream fact type name, with the name of the UI domain capability entry point (e.g redis stream channel ensuring main entrypoint of a capability domain) allowing to bridge the
 * event forwarding. The goal of a recipients list (e.g defining domain UI capability entrypoint channel for type of event) is to define the routing path in a generic way based on the capabilities api supporting the event naming between the API IO endpoint and the UIS space.
 * <p>
 * It's an implementation of architectural pattern named "Recipient List".
 * Generally, one common recipient channel (Redis entrypoint materializing domain capability IO) is supporting several event types.
 */
public class UISRecipientList {

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
    public UISRecipientList() {
        // Initialize the routing destination tables that link an event bus channel with
        // a redis channel
        routingMap = new HashMap<>();

        // Set each domain destination path supporting each type of authorized event
        //routingMap.put(CommandName.REGISTER_ORGANIZATION.name(), UICapabilityChannel.access_control_organization_registration);// Dedicated feature entrypoint supporting organization registration command
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
