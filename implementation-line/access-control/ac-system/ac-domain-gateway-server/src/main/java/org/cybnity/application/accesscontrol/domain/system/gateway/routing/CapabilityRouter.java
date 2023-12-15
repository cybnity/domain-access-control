package org.cybnity.application.accesscontrol.domain.system.gateway.routing;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

/**
 * Manager of routing services providing the routes exposed by the gateway.
 */
public interface CapabilityRouter extends Router {

    /**
     * Create a router supporting health HTTP protocol.
     *
     * @param vertx a Vert.x instance.
     * @return A router including routes with SockJS handlers.
     */
    static Router httpHealthRouter(Vertx vertx) {
        return new HealthHTTPRouterImpl(vertx);
    }
}
