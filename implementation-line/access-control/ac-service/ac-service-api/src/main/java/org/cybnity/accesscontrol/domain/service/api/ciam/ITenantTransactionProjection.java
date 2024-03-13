package org.cybnity.accesscontrol.domain.service.api.ciam;

import org.cybnity.accesscontrol.domain.service.api.model.TenantTransaction;
import org.cybnity.framework.domain.IReadModelProjection;
import org.cybnity.framework.domain.ISessionContext;

/**
 * Represents an optimized read-model projection allowing query and read of denormalized version of Tenant transactions.
 * This interface contract is covering a perimeter of Tenant read-model and provide TenantTransaction read capabilities.
 */
public interface ITenantTransactionProjection extends IReadModelProjection {

    /**
     * Search existing tenant which have an equals name.
     *
     * @param label Mandatory logical name of tenant to search.
     * @param isOperationStatus Optional operational status of the tenant to search.
     * @param ctx   Mandatory context.
     * @return An identified tenant, or null.
     * @throws IllegalArgumentException When any mandatory parameter is not valid.
     */
    public TenantTransaction findByLabel(String label, Boolean isOperationStatus, ISessionContext ctx) throws IllegalArgumentException;
}
