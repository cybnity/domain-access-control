package org.cybnity.feature.accesscontrol.domain.system;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.cybnity.feature.accesscontrol.domain.system.service.TenantRegistrationFeaturePipeline;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.application.vertx.common.WorkersManagementCapability;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.NamingConventionHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Start a composition of gateway Verticle supporting the application security
 * services provided by the processing units of the domain.
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_8310_AC2")
public class AccessControlDomainProcessModule extends AbstractVerticle {

	/**
	 * Generic helper providing basic reusable services regarding workers management.
	 */
	private final WorkersManagementCapability workersCapability = new WorkersManagementCapability();

	/**
	 * Technical logging
	 */
	private static final Logger logger = Logger.getLogger(AccessControlDomainProcessModule.class.getName());

	/**
	 * Utility class managing the verification of operable instance.
	 */
	private ExecutableACProcessModuleChecker healthyChecker;

	/**
	 * Name of the pool including all the executed workers managed by this computation module.
	 */
	private static final String PROCESSING_UNIT_MODULE_POOL_NAME = NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.PROCESSING_UNIT, /* domainName */ "ac", /* componentMainFunction */"process-module",/* resourceType */ null, /* segregationLabel */ "workers");

	/**
	 * Default start method regarding the server.
	 *
	 * @param args None pre-required.
	 */
	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();
		// Deploy health check support over http
		vertx.deployVerticle(new AccessControlDomainProcessModule()).onComplete(res -> {
			if (res.succeeded()) {
				logger.info("Access control (AC) domain Process Module deployed (id: " + res.result() + ")");
			} else {
				logger.info("Access control (AC) domain Process Module deployment failed!");
			}
		});
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		// Check the minimum required data allowing operating
		checkHealthyState();
		// Define the deployment options per feature worker type to start
		Map<String, DeploymentOptions> deployed = managedFeaturesWorkers();

		// Start all process module workers
		workersCapability.startWorkers(deployed, vertx, logger, "AC Features");

		// Create the HTTP server supporting supervision
		workersCapability.createHttpServer(getVertx(), logger, startPromise, "AC Features Processing Unit");
	}

	/**
	 * Prepare and get the set of domain feature workers managed by this processing unit.
	 *
	 * @return Map of workers providing features, or empty map.
	 */
	private Map<String, DeploymentOptions> managedFeaturesWorkers() {
		Map<String, DeploymentOptions> deployedWorkers = new HashMap<>();

		// Set each feature worker verticle as workers pool member
		DeploymentOptions options = workersCapability.baseDeploymentOptions(PROCESSING_UNIT_MODULE_POOL_NAME);

		// Define worker instances quantity per feature type managed by this module
		workersCapability.configureWorkerInstances(options);

		// Define threads pool size per feature type
		workersCapability.configureWorkerThreadsPoolSize(options);

		// --- EMBEDDED FEATURES DEFINITION ---
		// Add each feature worker to the set of workers providing services
		deployedWorkers.put(TenantRegistrationFeaturePipeline.class.getName(), options);

		return deployedWorkers;
	}

	/**
	 * Resource freedom (e.g un deployment of all worker instances).
	 */
	@Override
	public void stop() {
		// Undeploy each worker
		workersCapability.undeployWorkers(vertx);
	}

	/**
	 * Verify the status of health regarding this instance.
	 * @throws UnoperationalStateException When an issue is detected as cause of potential non stability source (e.g missing environment variable required during the runtime).
	 */
	public void checkHealthyState() throws UnoperationalStateException {
		if (healthyChecker == null)
			healthyChecker = new ExecutableACProcessModuleChecker(workersCapability.context);
		// Execute the health check
		healthyChecker.checkOperableState();
	}
}
