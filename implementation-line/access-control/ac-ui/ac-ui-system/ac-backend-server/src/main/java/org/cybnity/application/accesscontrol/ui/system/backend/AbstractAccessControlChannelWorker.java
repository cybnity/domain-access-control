package org.cybnity.application.accesscontrol.ui.system.backend;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

/**
 * Verticle supporting public channels exposed by the domain from an event bus entry point.
 * This abstract class can be redefined by sub-class providing public service or secured tasks with automatic access control check.
 */
public abstract class AbstractAccessControlChannelWorker extends AbstractVerticle {

    /**
     * This default implementation method start the observed channels (as entry points) and start this worker instance including the execution of the complete() action on the startPromise parameter.
     *
     * @param startPromise Mandatory promise.
     * @throws Exception
     */
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        // Start consumers listening th observed channels (as entry points) by this worker
        startChannelConsumers();
        startPromise.complete();
    }

    /**
     * Start consumers managed by this worker instance and their observation of entrypoint(s).
     */
    abstract protected void startChannelConsumers();

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        super.stop(stopPromise);
    }
}
