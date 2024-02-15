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
     * Search existing tenant which have an equals name relative to a specific label.
     *
     * @param name Mandatory logical name of tenant to search as owner of a known tenant.
     * @return An identified tenant, or null.
     * @throws IllegalArgumentException When any mandatory parameter is not valid.
     */
    public Tenant findByName(String name) throws IllegalArgumentException;

}
