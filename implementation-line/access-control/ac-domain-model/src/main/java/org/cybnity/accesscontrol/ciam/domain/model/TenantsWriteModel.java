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
     * Register a new Tenant into the Tenants registry which is declared as represent of an organization.
     *
     * @param organizationName Mandatory name of the organization owning the tenant to create.
     * @return Instance of new created tenant.
     * @throws IllegalArgumentException When any mandatory parameter is not valid.
     */
    public Tenant createTenant(String organizationName) throws IllegalArgumentException;
}
