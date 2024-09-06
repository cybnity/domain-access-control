package org.cybnity.accesscontrol.domain.infrastructure.impl.projections;

import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.infrastructure.IDomainStore;
import org.cybnity.framework.domain.model.IDomainModel;
import org.cybnity.framework.domain.model.ITransactionStateObserver;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.infrastructure.technical.registry.repository.impl.janusgraph.AbstractDomainGraphImpl;
import org.cybnity.infrastructure.technical.registry.repository.impl.janusgraph.AbstractReadModelProjectionsSet;

/**
 * Projections collection relative to the AC domain perimeter or to a specific domain object type (e.g a domain aggregate).
 */
public class AccessControlDomainReadModelImpl extends AbstractReadModelProjectionsSet {
    /**
     * Default constructor.
     *
     * @param ctx                     Mandatory context.
     * @param rootGraph               Mandatory origin graph that is manipulated by this read-model projections.
     * @param readModelOwnership      Mandatory owner of the perimeter of this read-model in terms of data-view responsibility scope.
     * @param readModelChangeObserver Optional observer of changes occurred onto the real-model projection (e.g at end of data view transaction execution).
     * @param tenantsWriteModelStore  Mandatory rehydration responsible for tenant domain objects.
     * @throws UnoperationalStateException When problem during context usage.
     * @throws IllegalArgumentException    When any mandatory parameter is missing.
     */
    public AccessControlDomainReadModelImpl(IContext ctx, AbstractDomainGraphImpl rootGraph, IDomainModel readModelOwnership, ITransactionStateObserver readModelChangeObserver, IDomainStore<Tenant> tenantsWriteModelStore) throws UnoperationalStateException, IllegalArgumentException {
        super(ctx);
        // Initialize read model scope
        initDataViewProjections(rootGraph, readModelOwnership, readModelChangeObserver, tenantsWriteModelStore);
    }


    /**
     * Initialize the perimeter of data views managed by the Access Control domain implementation model via projections set.
     *
     * @param rootGraph              Mandatory origin graph.
     * @param ownership              Mandatory owner of the data-view projections perimeter.
     * @param observer               Optional observer of the transaction state evolution (e.g to be notified about progress or end of performed transaction).
     * @param tenantsWriteModelStore Mandatory rehydration responsible for tenant domain objects.
     * @throws IllegalArgumentException    When mandatory parameter is missing.
     * @throws UnoperationalStateException When problem during an attempt of prepared projection activation.
     */
    private void initDataViewProjections(AbstractDomainGraphImpl rootGraph, IDomainModel ownership, ITransactionStateObserver observer, IDomainStore<Tenant> tenantsWriteModelStore) throws IllegalArgumentException, UnoperationalStateException {
        // Prepare the set of managed projections defining this read model perimeter that is maintained onto the root graph for STATE UPDATE TRANSACTIONS
        if (tenantsWriteModelStore == null)
            throw new IllegalArgumentException("tenantsWriteModelStore parameter is required!"); // Need by TenantDataViewState projections
        this.addProjection(new TenantDataViewStateTransactionImpl(ownership, rootGraph, observer, tenantsWriteModelStore /* See initSupportedTransactions() method where an observed store is required as defined during instance construction */));

        // ... other projections relative to other objects type and/or relations managed by this read-model perimeter (Access Control domain)
    }

}
