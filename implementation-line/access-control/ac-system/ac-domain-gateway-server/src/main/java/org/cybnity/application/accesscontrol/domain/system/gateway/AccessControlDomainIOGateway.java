package org.cybnity.application.accesscontrol.domain.system.gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.cybnity.application.accesscontrol.domain.system.gateway.routing.CapabilityRouter;
import org.cybnity.application.accesscontrol.domain.system.gateway.service.DomainIOEventsPipeline;
import org.cybnity.framework.Context;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Start a composition of gateway Verticle supporting the identification of command supported by this domain and distribution to processing units (e.g UI capability)
 * and ensuring the forwarding of domain events to UI layer (e.g domain reactive messaging gateway).
 */
public class AccessControlDomainIOGateway extends AbstractVerticle {

    /**
     * List of identifiers regarding deployed verticles.
     */
    private final List<String> deploymentIDs = new LinkedList<>();

    /**
     * Current context of adapter runtime.
     */
    private final IContext context = new Context();

    /**
     * Technical logging
     */
    private static final Logger logger = Logger.getLogger(AccessControlDomainIOGateway.class.getName());

    /**
     * Utility class managing the verification of operable instance.
     */
    private ExecutableIOGatewayChecker healthyChecker;

    /**
     * Name of the pool including all the executed workers of this domain.
     */
    private static final String DOMAIN_POOL_NAME = "access-control-io-workers";

    /**
     * Default start method regarding the server.
     *
     * @param args None pre-required.
     */
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        // Deploy health check support over http
        vertx.deployVerticle(new AccessControlDomainIOGateway()).onComplete(res -> {
            if (res.succeeded()) {
                logger.info("Access control (AC) domain IO Gateway deployed (id: " + res.result() + ")");
            } else {
                logger.info("Access control (AC) domain IO Gateway deployment failed!");
            }
        });
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // Check the minimum required data allowing operating
        checkHealthyState();
        // Define the api IO pipeline to start
        Map<String, DeploymentOptions> deployed = capabilityAPIIOPipelineWorkers();

        // Start all gateway workers
        for (Map.Entry<String, DeploymentOptions> entry :
                deployed.entrySet()) {
            vertx.deployVerticle(entry.getKey(), entry.getValue())
                    .onComplete(res -> {
                        if (res.succeeded()) {
                            // Save undeployable verticle identifier
                            deploymentIDs.add(res.result());
                            logger.info(entry.getValue().getInstances() + " AC IO worker instances deployed (type: " + entry.getKey() + ", id: " + res.result() + ")");
                        } else {
                            logger.info("AC IO worker instances deployment failed!");
                        }
                    });
        }

        // HTTP specific request handler
        // Create a Router initialized to health supervision routes
        Router router = CapabilityRouter.httpHealthRouter(vertx);

        // Create the HTTP server supporting supervision
        getVertx().createHttpServer()
                // Handle every request using the router
                .requestHandler(router)
                // Start HTTP listening according to the application settings
                .listen(Integer
                        .parseInt(context.get(AppConfigurationVariable.ENDPOINT_HTTP_SERVER_PORT)))
                // Print the port
                .onSuccess(server -> {
                    logger.info("AC IO Gateway server started (port: " + server.actualPort() + ")");
                    startPromise.complete();
                }).onFailure(error -> {
                    logger.info("AC IO Gateway server start failure: " + error.toString());
                    startPromise.fail(error);
                });
    }


    /**
     * Prepare and get the set of domain capability public api workers managed by this gateway.
     *
     * @return Map of workers ensuring a capabilities api of services, or empty map.
     */
    private Map<String, DeploymentOptions> capabilityAPIIOPipelineWorkers() {
        Map<String, DeploymentOptions> deployedWorkers = new HashMap<>();

        // Set each domain worker verticle at workers pool
        DeploymentOptions options = baseDeploymentOptions();

        // Define instances quantity for this worker type
        configureWorkerInstances(options);

        // Define worker threads pool size
        configureWorkerThreadsPoolSize(options);

        // Add workers to the set of workers serving public capabilities API without access control check
        deployedWorkers.put(DomainIOEventsPipeline.class.getName(), options);

        return deployedWorkers;
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
     * Prepare and return a basic deployment options including common parameters defined (e.g capability domain pool name).
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
            healthyChecker = new ExecutableIOGatewayChecker(context);
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

}
