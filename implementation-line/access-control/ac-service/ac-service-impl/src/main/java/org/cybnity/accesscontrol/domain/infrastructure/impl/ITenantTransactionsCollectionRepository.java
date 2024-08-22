package org.cybnity.accesscontrol.domain.infrastructure.impl;

import org.cybnity.accesscontrol.domain.service.api.model.TenantTransactionsCollection;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.infrastructure.IDomainRepository;

/**
 * Repository read-model perimeter providing TenantTransaction collections.
 */
public interface ITenantTransactionsCollectionRepository extends IDomainRepository<TenantTransactionsCollection> {

    /**
     * Drops the repository schema and data of the graph instance.
     *
     * @throws UnoperationalStateException Problem occurred during the attempt to close the graph or to perform the schema/data deletion.
     */
    public void drop() throws UnoperationalStateException;
}
