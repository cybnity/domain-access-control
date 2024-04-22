package org.cybnity.accesscontrol.ciam.domain.model;

import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.IWriteModel;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.ImmutabilityException;

/**
 * Represents a persistence-oriented and query optimized repository (also sometimes called Aggregate
 * store, or Aggregate-Oriented database) contract for the Tenant bounded context.
 * Persistence capabilities regarding organization subscriptions as Tenants that allow to define a scope of multi-tenants application regarding named organizations.
 *
 * @author olivier
 */
public interface TenantsWriteModel extends IWriteModel {

    /**
     * Add change events into the tenants model as modified domain object, which is saved into the tenants store.
     * When previous instance already exist regarding the same tenant version, it is enhanced.
     * When none is existing, a new tenant is created.
     *
     * @param fact Mandatory fact to append.
     * @throws IllegalArgumentException    When mandatory parameter is missing.
     * @throws ImmutabilityException       When problem of immutable version of stored event is occurred.
     * @throws UnoperationalStateException When technical problem is occurred regarding this model usage.
     */
    public void add(Tenant fact) throws IllegalArgumentException, ImmutabilityException, UnoperationalStateException;
}
