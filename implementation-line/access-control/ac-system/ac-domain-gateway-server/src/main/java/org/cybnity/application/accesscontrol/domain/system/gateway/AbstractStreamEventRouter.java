package org.cybnity.application.accesscontrol.domain.system.gateway;

import io.vertx.core.AbstractVerticle;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.MessageMapperFactory;

/**
 * Verticle supporting stream exposed by the domain from Users Interactions Space entry point.
 * This abstract class can be redefined by subclass providing public service or secured tasks with automatic control check (e.g access control, IO event conformity & value check, transport metadata check).
 * It's an implementation of the Message Router architectural pattern that consumes a FactEvent from one stream (Redis's stream) and dispatches it to a specific domain capability feature (e.g domain UI capability) or Application Domain (e.g domain application layer entry point) depending on a set of conditions.
 */
public abstract class AbstractStreamEventRouter extends AbstractVerticle {

    /**
     * This default implementation method start the observed stream (as entry point) and start this worker instance including the execution of the complete() action on the startPromise parameter.
     */
    @Override
    public void start() {
        // Start consumers listening the observed streams (as entry points) by this router
        startStreamConsumers();
    }

    /**
     * Resource freedom (e.g undeployment of all verticles).
     */
    @Override
    public void stop() {
        // Stop the streams consumers listening
        stopStreamConsumers();
    }

    /**
     * Start consumers managed by this worker instance and their observation points.
     */
    abstract protected void startStreamConsumers();

    /**
     * Stop listeners managed by this worker instance and their observation points.
     */
    abstract protected void stopStreamConsumers();

    /**
     * Get a factory of message mapper that support the common message and event types supported by the domain, and/or the custom types specific to the domain only.
     *
     * @return A message mapper provider supporting custom and/or common event types of fact and messages.
     */
    abstract protected MessageMapperFactory getDomainMessageMapperProvider();
}
