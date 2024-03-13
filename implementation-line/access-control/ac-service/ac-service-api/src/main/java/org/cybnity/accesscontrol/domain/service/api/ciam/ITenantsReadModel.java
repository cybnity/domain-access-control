package org.cybnity.accesscontrol.domain.service.api.ciam;

import org.cybnity.framework.domain.IReadModel;
import org.cybnity.framework.domain.IReadModelProjection;

/**
 * Represents a read-oriented and query optimized repository relative to the Tenant aggregate versions scope.
 * This perimeter of read-model is bounded context oriented.
 *
 * @author olivier
 */
public interface ITenantsReadModel extends IReadModel {

    /**
     * Get a projection type supported by this read model.
     *
     * @param projectionProvided Mandatory type of projection which is supported by the read-model.
     * @return An available projection, or null when type is not found from the tenant read model.
     */
    public IReadModelProjection getProjection(Class<? extends IReadModelProjection> projectionProvided);
}
