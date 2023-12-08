package org.cybnity.application.accesscontrol.ui.system.backend;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.cybnity.application.accesscontrol.ui.system.backend.routing.CapabilityRouter;
import org.cybnity.application.accesscontrol.ui.system.backend.service.DomainPublicAPIMessagesRouter;
import org.cybnity.framework.Context;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Gateway ensuring deployment of supervision http routing (e.g supporting health control) and workers pool of domain capabilities handlers.
 * This component implement the Access Control Layer (ACL) regarding the domain UI capabilities (as UI API) over event bus protocol supported as domain's entry points.
 */
public class AccessControlMessagingGateway extends AbstractVerticle {

    /**
     * List of identifiers regarding deployed verticles.
     */
    private final List<String> deploymentIDs = new LinkedList<>();

    /**
     * Utility class managing the verification of operable instance.
     */
    private ExecutableBackendChecker healthyChecker;

    /**
     * Current context of adapter runtime.
     */
    private final IContext context = new Context();

    /**
     * Name of the pool including all the executed workers of this domain.
     */
    private static final String DOMAIN_POOL_NAME = "access-control-workers";

    /**
     * Technical logging
     */
    private static final Logger logger = Logger.getLogger(AccessControlMessagingGateway.class.getName());

    /**
     * Default start method regarding the server.
     *
     * @param args None pre-required.
     */
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        // Deploy health check support over http
        vertx.deployVerticle(new AccessControlMessagingGateway()).onComplete(res -> {
            if (res.succeeded()) {
                logger.info("Access control (AC) Messaging Gateway deployed (id: " + res.result() + ")");
            } else {
                logger.info("Access control (AC) Messaging Gateway deployment failed!");
            }
        });
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // Check the minimum required data allowing operating
        checkHealthyState();
        // Define the public api to start
        Map<String, DeploymentOptions> deployed = publicAPIWorkers();
        // Define the secure api to start
        deployed.putAll(secureAPIWorkers());

        // Start all gateway workers
        for (Map.Entry<String, DeploymentOptions> entry :
                deployed.entrySet()) {
            vertx.deployVerticle(entry.getKey(), entry.getValue())
                    .onComplete(res -> {
                        if (res.succeeded()) {
                            // Save undeployable verticle identifier
                            deploymentIDs.add(res.result());
                            logger.info(entry.getValue().getInstances() + " AC worker instances deployed (type: " + entry.getKey() + ", id: " + res.result() + ")");
                        } else {
                            logger.info("AC worker instances deployment failed!");
                        }
                    });
        }


        // HTTP specific request handler
        // Create a Router initialized to support routes
        Router router = CapabilityRouter.httpRouter(vertx, context);

        // Create the HTTP server supporting supervision
        getVertx().createHttpServer()
                // Handle every request using the router
                .requestHandler(router)
                // Start HTTP listening according to the application settings
                .listen(Integer
                        .parseInt(context.get(AppConfigurationVariable.REACTIVE_BACKEND_ENDPOINT_HTTP_SERVER_PORT)))
                // Print the port
                .onSuccess(server -> {
                    logger.info("AC Messaging Gateway server started (port: " + server.actualPort() + ")");
                    startPromise.complete();
                }).onFailure(error -> {
                    logger.info("AC Messaging Gateway server start failure: " + error.toString());
                    startPromise.fail(error);
                });
    }

    /**
     * Resource freedom (e.g undeployment of all verticles).
     */
    @Override
    public void stop() {
        // Undeploy each worker
        for (String deploymentId : deploymentIDs) {
            vertx.undeploy(deploymentId);
        }
    }

    public void checkHealthyState() throws UnoperationalStateException {
        if (healthyChecker == null)
            healthyChecker = new ExecutableBackendChecker(context);
        // execution the health check
        healthyChecker.checkOperableState();
    }

    /**
     * Define instances quantity for this worker type according to existing AppConfigurationVariable.DOMAIN_WORKER_INSTANCES environment variable, and add configuration to the deployment options set.
     * When environment variable is not defined, none configuration about instances quantity is configured.
     *
     * @param options Mandatory options to enhance. This method make nothing if null parameter.
     */
    private void configureWorkerInstances(DeploymentOptions options) {
        if (options != null) {
            // Define instances quantity per worker type
            String workerInstances = context.get(AppConfigurationVariable.DOMAIN_WORKER_INSTANCES);
            if (!"".equalsIgnoreCase(workerInstances))
                options.setInstances(Integer.parseInt(workerInstances));
        }
    }

    /**
     * Define instances quantity for this worker type according to existing AppConfigurationVariable.DOMAIN_WORKER_THREAD_POOL_SIZE environment variable, and add configuration to the deployment options set.
     * When environment variable is not defined, none configuration about instances quantity is configured.
     *
     * @param options Mandatory options to enhance. This method make nothing if null parameter.
     */
    private void configureWorkerThreadsPoolSize(DeploymentOptions options) {
        if (options != null) {
            // Define worker threads pool size
            String workerInstances = context.get(AppConfigurationVariable.DOMAIN_WORKER_THREAD_POOL_SIZE);
            if (!"".equalsIgnoreCase(workerInstances))
                options.setInstances(Integer.parseInt(workerInstances));
        }
    }

    /**
     * Prepare and get the set of public api workers managed by this gateway.
     *
     * @return Map of workers ensuring a public api of services, or empty map.
     */
    private Map<String, DeploymentOptions> publicAPIWorkers() {
        Map<String, DeploymentOptions> deployedPublicWorkers = new HashMap<>();

        // Set each domain worker verticle at workers pool
        DeploymentOptions options = baseDeploymentOptions();

        // Define instances quantity for this worker type
        configureWorkerInstances(options);

        // Define worker threads pool size
        configureWorkerThreadsPoolSize(options);

        // Add worker to the set of workers serving public UI API without access control check
        deployedPublicWorkers.put(DomainPublicAPIMessagesRouter.class.getName(), options);

        return deployedPublicWorkers;
    }

    /**
     * Prepare and return a basic deployment options including common parameters defined (e.g domain pool name).
     *
     * @return A option set.
     */
    private DeploymentOptions baseDeploymentOptions() {
        // A worker is just like a standard Verticle, but it’s executed using a thread from the Vert.x worker thread pool, rather than using an event loop.
        // Workers are designed for calling blocking code, as they won’t block any event loops
        DeploymentOptions options = new DeploymentOptions().setWorker(true);
        options.setWorkerPoolName(DOMAIN_POOL_NAME);
        return options;
    }

    /**
     * Prepare and get the set of secure api workers managed by this gateway.
     *
     * @return Map of workers ensuring a secured api of services, or empty map.
     */
    private Map<String, DeploymentOptions> secureAPIWorkers() {
        Map<String, DeploymentOptions> deployedSecureWorkers = new HashMap<>();

        // Set each domain worker Verticle at workers pool
        DeploymentOptions options = baseDeploymentOptions();

        // Define instances quantity for this worker type
        configureWorkerInstances(options);

        // Define worker threads pool size
        configureWorkerThreadsPoolSize(options);

        // Add worker to set of workers serving secure UI API with access control check

        return deployedSecureWorkers;
    }
}
