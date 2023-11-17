package org.cybnity.application.accesscontrol.ui.system.backend.routing;

import io.vertx.core.AsyncResult;

/**
 * Generic result of resource access authorization performed and resulting as a valid authorized status or rejected for a cause.
 *
 * @param <T>
 */
public class ResourceAccessAuthorizationResult<T> implements AsyncResult<Boolean> {

    private final Boolean status;
    private final Throwable cause;

    public ResourceAccessAuthorizationResult(Boolean succeeded, Throwable cause) {
        this.status = succeeded;
        this.cause = cause;
    }

    @Override
    public Boolean result() {
        return status;
    }

    @Override
    public Throwable cause() {
        return this.cause;
    }

    @Override
    public boolean succeeded() {
        return this.status.booleanValue();
    }

    @Override
    public boolean failed() {
        return (!this.status);
    }

}
