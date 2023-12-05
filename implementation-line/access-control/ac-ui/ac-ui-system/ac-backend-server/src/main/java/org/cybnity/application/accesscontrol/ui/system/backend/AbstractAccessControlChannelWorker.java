package org.cybnity.application.accesscontrol.ui.system.backend;

import io.vertx.core.AbstractVerticle;

/**
 * Verticle supporting channels exposed by the domain from an event bus entry point.
 * This abstract class can be redefined by subclass providing public service or secured tasks with automatic access control check.
 */
public abstract class AbstractAccessControlChannelWorker extends AbstractVerticle {

    /**
     * This default implementation method start the observed channels (as entry points) and start this worker instance including the execution of the complete() action on the startPromise parameter.
     */
    @Override
    public void start() {
        // Start consumers listening th observed channels (as entry points) by this worker
        startChannelConsumers();
    }

    /**
     * Resource freedom (e.g undeployment of all verticles).
     */
    @Override
    public void stop() {
        // Stop the channel consumers listening
        stopChannelConsumers();
    }

    /**
     * Start consumers managed by this worker instance and their observation points.
     */
    abstract protected void startChannelConsumers();

    /**
     * Stop listeners managed by this worker instance and their observation points.
     */
    abstract protected void stopChannelConsumers();
}
