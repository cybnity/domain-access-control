package org.cybnity.application.accesscontrol.ui.system.backend;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.cybnity.application.accesscontrol.ui.system.backend.routing.CapabilityRouter;
import org.cybnity.framework.Context;
import org.cybnity.framework.IContext;
import io.vertx.core.Promise;
import org.cybnity.framework.UnoperationalStateException;

/**
 * Start a composition of backend Verticle supporting the AC UI modules.
 */
public class AccessControlBackendServer extends AbstractVerticle {

    /**
     * Current context of adapter runtime.
     */
    private final IContext context = new Context();
    /**
     * Utility class managing the verification of operable instance.
     */
    private ExecutableBackendChecker healthyChecker;

    /**
     * Default start method regarding the server.
     *
     * @param args None pre-required.
     */
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new AccessControlBackendServer());
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // Check the minimum required data allowing operating
        checkHealthyState();
        // Route event bus's events to specific request handlers

        // Create a Router initialized to support capability routes
        Router router = CapabilityRouter.httpRouter(vertx, context);

        // Create the HTTP server
        getVertx().createHttpServer()
                // Handle every request using the router
                .requestHandler(router)
                // Start HTTP listening according to the application settings
                .listen(Integer
                        .parseInt(context.get(AppConfigurationVariable.REACTIVE_BACKEND_ENDPOINT_HTTP_SERVER_PORT)))
                // Print the port
                .onSuccess(server -> {
                    System.out.println("Access Control domain UI backend server started (port: " + server.actualPort() + ")");
                    startPromise.complete();
                }).onFailure(error -> {
                    System.out.println("Access Control backend server start failure: " + error.getCause());
                    startPromise.fail(error.getCause());
                });
    }

    /**
     * Verify the current status of this component as healthy and operable.
     *
     * @throws UnoperationalStateException When missing required contents (e.g
     *                                     environment variables).
     */
    public void checkHealthyState() throws UnoperationalStateException {
        if (healthyChecker == null)
            healthyChecker = new ExecutableBackendChecker(context);
        // execution the health check
        healthyChecker.checkOperableState();
    }

}