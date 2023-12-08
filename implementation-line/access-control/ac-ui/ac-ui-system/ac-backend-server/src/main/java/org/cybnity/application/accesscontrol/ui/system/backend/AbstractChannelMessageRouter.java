package org.cybnity.application.accesscontrol.ui.system.backend;

import io.vertx.core.AbstractVerticle;

/**
 * Verticle supporting channels exposed by the domain from an event bus entry point.
 * This abstract class can be redefined by subclass providing public service or secured tasks with automatic access control check.
 * It's an implementation of the Message Router architectural pattern that consumes a Message from one channel (Event bus's channel) and republishes it to a different UIS recipient (e.g domain UI capability entry point) depending on a set of conditions.
 */
public abstract class AbstractChannelMessageRouter extends AbstractVerticle {

    /**
     * This default implementation method start the observed channels (as entry points) and start this worker instance including the execution of the complete() action on the startPromise parameter.
     */
    @Override
    public void start() {
        // Start consumers listening th observed channels (as entry points) by this router
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
