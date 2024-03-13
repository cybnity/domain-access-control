package org.cybnity.accesscontrol.ciam.domain.model;

import org.cybnity.framework.domain.IWriteModel;
import org.cybnity.framework.domain.model.Tenant;

/**
 * Represents a persistence-oriented and query optimized repository (also sometimes called Aggregate
 * store, or Aggregate-Oriented database) contract for the Tenant bounded context.
 * Persistence capabilities regarding organization subscriptions as Tenants that allow to define a scope of multi-tenants application regarding named organizations.
 *
 * @author olivier
 */
public interface TenantsWriteModel extends IWriteModel {

    /**
     * Add change events into the tenant stream as modified domain object, which is saved into the tenants store.
     * When previous stream already exist regarding the same tenant stream version, it is enhanced.
     * When none is existing, a new tenant stream is created and
     *
     * @param fact Mandatory fact to append.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public void appendToStream(Tenant fact) throws IllegalArgumentException;
}
