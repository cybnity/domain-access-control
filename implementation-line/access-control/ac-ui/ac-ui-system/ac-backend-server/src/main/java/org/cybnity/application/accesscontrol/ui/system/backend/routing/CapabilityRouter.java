package org.cybnity.application.accesscontrol.ui.system.backend.routing;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.cybnity.framework.IContext;

/**
 * Manager of routing services providing the routes exposed by the backend.
 */
public interface CapabilityRouter extends Router {

    /**
     * Create a router supporting HTTP protocol.
     *
     * @param vertx a Vert.x instance.
     * @param ctx   An environment context
     * @return A router including routes with SockJS handlers.
     */
    static Router httpRouter(Vertx vertx, IContext ctx) {
        return new UICapabilitiesHTTPRouterImpl(vertx, ctx);
    }
}
