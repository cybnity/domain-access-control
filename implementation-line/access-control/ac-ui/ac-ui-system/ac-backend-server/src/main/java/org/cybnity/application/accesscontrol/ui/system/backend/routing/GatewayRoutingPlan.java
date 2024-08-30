package org.cybnity.application.accesscontrol.ui.system.backend.routing;

import org.cybnity.application.accesscontrol.translator.ui.api.UICapabilityChannel;
import org.cybnity.application.accesscontrol.ui.api.event.CommandName;
import org.cybnity.framework.application.vertx.common.routing.RouteRecipientList;

/**
 * Identify the correct recipient based on a message's content.
 * A recipient channel is defined for each event type supported by the domain.
 * Identify each event bus fact type name, with the name of the UI domain capability entry point (e.g redis stream channel ensuring main entrypoint of a capability domain) allowing to bridge the
 * event forwarding. The goal of a recipients list (e.g defining domain UI capability entrypoint channel for type of message) is to define (on server-side and hidden from the client side
 * source code's url and/or javascript) the routing path in a generic way based on the capabilities api supporting the event naming between the event bus and the Redis space.
 * <p>
 * It's an implementation of architectural pattern named "Recipient List".
 * Generally, one common recipient channel (Redis entrypoint materializing domain capability API) is supporting several event types.
 */
public class GatewayRoutingPlan extends RouteRecipientList {

    /**
     * Default constructor initializing the routing table. This configuration
     * implementation example of linked event types could be replaced by a routing
     * configuration file that is easy to maintain (e.g with possible hot change
     * supported) with settings hosted by the domains-interactions-broker module (e.g
     * as configuration api).
     */
    public GatewayRoutingPlan() {
        super();

        // Set each domain destination path supporting each type of authorized message
        addRoute(CommandName.REGISTER_TENANT.name(), UICapabilityChannel.access_control_in.shortName());// Global entrypoint supporting organization registration command
    }

}
