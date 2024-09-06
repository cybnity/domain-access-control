package org.cybnity.accesscontrol.domain.infrastructure.impl;

import org.cybnity.framework.domain.DomainEvent;

/**
 * Represent a manager of Tenant aggregate change events which is responsible to build a Tenant read-model projection and maintain it up-to-date.
 * Similar to an Aggregate instance, this Projection is receiving and handling the events relative to Tenant (e.g when Tenant events store is modified),
 * and ensure the build of Projection's state.
 * Read Model Projections are persisted after each update and can be accessed by many readers, both inside and outside the Access Control bounded context.
 */
public abstract class AbstractTenantsProjection {

    /**
     * Capture a transaction relative to a Tenant changed including a type of event definition.
     *
     * @param tenantChanged Committed change.
     */
    public abstract void when(DomainEvent tenantChanged);
}
