package org.cybnity.application.accesscontrol.domain.system.gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.cybnity.application.accesscontrol.domain.system.gateway.service.DomainIOEventsPipeline;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.application.vertx.common.WorkersManagementCapability;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.NamingConventionHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Start a composition of gateway Verticle supporting the identification of command supported by this domain and distribution to processing units (e.g UI capability)
 * and ensuring the forwarding of domain events to UI layer (e.g domain reactive messaging gateway).
 */
public class AccessControlDomainIOGateway extends AbstractVerticle {

    /**
     * Generic helper providing basic reusable services regarding workers management.
     */
    private final WorkersManagementCapability workersCapability = new WorkersManagementCapability();

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
    private static final String DOMAIN_POOL_NAME = NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.GATEWAY, /* domainName */ "ac", /* componentMainFunction */"io",/* resourceType */ null, /* segregationLabel */ "workers");

    /**
     * Unique logical name of this gateway domain.
     */
    private static final String GW_DOMAIN_NAME = "AC IO";

    /**
     * Unique logical name of this processing module.
     */
    private static final String PU_LOGICAL_NAME = GW_DOMAIN_NAME + " Gateway";

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

    /**
     * Start of workers and HTTP service.
     */
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // Check the minimum required data allowing operating
        checkHealthyState();
        // Define the deployment options per api pipeline type to start
        Map<String, DeploymentOptions> deployed = capabilityAPIIOPipelineWorkers();

        // Start all gateway workers
        workersCapability.startWorkers(deployed, vertx, logger, GW_DOMAIN_NAME);

        // Create the HTTP server supporting supervision
        workersCapability.createHttpServer(getVertx(), logger, startPromise, PU_LOGICAL_NAME);
    }

    /**
     * Resource freedom (e.g undeploy all worker instances and stop http service).
     */
    @Override
    public void stop() {
        // Stop HTTP server
        workersCapability.stopHttpServer(logger, null, PU_LOGICAL_NAME);
        // Undeploy each worker
        workersCapability.undeployWorkers(vertx);
    }

    /**
     * Prepare and get the set of domain capability api workers managed by this gateway.
     *
     * @return Map of workers exposing the capabilities api of services, or empty map.
     */
    private Map<String, DeploymentOptions> capabilityAPIIOPipelineWorkers() {
        Map<String, DeploymentOptions> deployedWorkers = new HashMap<>();

        // Set each domain worker verticle as workers pool member
        DeploymentOptions options = workersCapability.baseDeploymentOptions(DOMAIN_POOL_NAME);

        // Define instances quantity for this worker type
        workersCapability.configureWorkerInstances(options);

        // Define worker threads pool size
        workersCapability.configureWorkerThreadsPoolSize(options);

        // Add each pipelined IO component type to the set of workers serving capabilities API with or without access control check
        deployedWorkers.put(DomainIOEventsPipeline.class.getName(), options);

        return deployedWorkers;
    }

    /**
     * Verify the status of health regarding this instance.
     * @throws UnoperationalStateException When an issue is detected as cause of potential non stability source (e.g missing environment variable required during the runtime).
     */
    public void checkHealthyState() throws UnoperationalStateException {
        if (healthyChecker == null)
            healthyChecker = new ExecutableIOGatewayChecker(workersCapability.context);
        // Execute the health check
        healthyChecker.checkOperableState();
    }

}
