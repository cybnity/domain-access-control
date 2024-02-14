package org.cybnity.accesscontrol.ciam.domain.model;

import org.cybnity.framework.domain.IReadModel;
import org.cybnity.framework.domain.model.Tenant;

/**
 * Represents a read-oriented and query optimized repository (also sometimes called Aggregate
 * store, or Aggregate-Oriented database) contract for the Tenant bounded context.
 *
 * @author olivier
 */
public interface TenantsReadModel extends IReadModel {

    /**
     * Search existing tenant which have an equals name relative to an organization owner.
     *
     * @param organizationName    Mandatory name of organization to search as owner of a known tenant.
     * @param includingExistUsers Is tenant to search must shall be filtered according any existing valid (e.g registered and activated as verified user account) user accounts already managed? True if tenant to return shall be a tenant that have valid user accounts already activated. False when the search shall ignore the potential existing of valid user accounts activated. Ignored filter when null.
     * @return An identified tenant, or null.
     * @throws IllegalArgumentException When any mandatory parameter is not valid.
     */
    public Tenant findTenant(String organizationName, Boolean includingExistUsers) throws IllegalArgumentException;

}
