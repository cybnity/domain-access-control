package org.cybnity.feature.accesscontrol.domain.system;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.cybnity.application.accesscontrol.ui.api.experience.ExecutionResource;
import org.cybnity.framework.Context;
import org.cybnity.framework.IContext;
import org.cybnity.framework.application.vertx.common.WorkersManagementCapability;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.NamingConventionHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Start a composition of gateway Verticle supporting the application security
 * services provided by the processing units of the domain.
 */
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
	 * Name of the pool including all the executed workers of this module.
	 */
	private static final String PROCESS_MODULE_POOL_NAME = NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.PROCESSING_UNIT, /* domainName */ "ac", /* componentMainFunction */"process-module",/* resourceType */ null, /* segregationLabel */ "workers");

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

		System.out.println("Access Control computation unit is started");
		startPromise.complete();
		// TODO ajouter l'envoi de l'announced par usage de ProcessingUnitPresenceAnnouncedEventFactory

		/*vertx.deployVerticle(
				Set each feature unit regarding this domain api
				CreateAssetFeature.class.getName(), event -> {
					if (event.succeeded()) {
						System.out.println(
								CreateAssetFeature.class.getSimpleName() + " successly deployed: " + event.result());
						startPromise.complete();
					} else {
						System.out.println(CreateAssetFeature.class.getSimpleName() + " deployment failed: ");
						event.cause().printStackTrace();
						startPromise.fail(event.cause());
					}
				});*/
	}
}
