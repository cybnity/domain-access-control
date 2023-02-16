package org.cybnity.application.access_control.domain.system.gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

/**
 * Start a composition of gateway Verticle supporting the Application security
 * services provided by the processing units of the domain.
 */
public class AccessControlDomainIOGateway extends AbstractVerticle {

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		/*vertx.deployVerticle(
				/* Set the routing manager regarding this domain api */
				/*AccessControlSecurityFeaturesDispatcher.class.getName(), event -> {
					if (event.succeeded()) {
						System.out.println(AccessControlSecurityFeaturesDispatcher.class.getSimpleName()
								+ " successly deployed: " + event.result());
						startPromise.complete();
					} else {
						System.out.println(
								AccessControlSecurityFeaturesDispatcher.class.getSimpleName() + " deployment failed: ");
						event.cause().printStackTrace();
						startPromise.fail(event.cause());
					}
				});
				*/

			System.out.println("Access Control domain IO Gateway module is started");
			startPromise.complete();
	}
}
