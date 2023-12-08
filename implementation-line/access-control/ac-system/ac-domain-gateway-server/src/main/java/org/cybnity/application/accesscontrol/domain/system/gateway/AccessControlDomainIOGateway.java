package org.cybnity.application.accesscontrol.domain.system.gateway;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

/**
 * Start a composition of gateway Verticle supporting the identification of command supported by this domain and distribution to processing units (e.g UI capability)
 * and ensuring the forwarding of domain events to UI layer (e.g domain reactive messaging gateway).
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
