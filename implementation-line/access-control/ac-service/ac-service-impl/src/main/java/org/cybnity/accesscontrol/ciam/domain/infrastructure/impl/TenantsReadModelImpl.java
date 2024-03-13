package org.cybnity.accesscontrol.ciam.domain.infrastructure.impl;

import org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.projections.TenantLabelOptimizedProjectionImpl;
import org.cybnity.accesscontrol.domain.service.api.ciam.ITenantsReadModel;
import org.cybnity.accesscontrol.domain.service.api.model.TenantTransactionsCollection;
import org.cybnity.framework.domain.DomainEvent;
import org.cybnity.framework.domain.IReadModelProjection;
import org.cybnity.framework.domain.infrastructure.IDomainRepository;
import org.cybnity.framework.domain.infrastructure.IDomainStore;
import org.cybnity.framework.domain.model.DomainEventSubscriber;
import org.cybnity.framework.domain.model.EventStore;
import org.cybnity.framework.domain.model.Tenant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provider of Tenants read-model projections supported by the Tenant scope.
 */
public class TenantsReadModelImpl implements ITenantsReadModel {

    /**
     * Collection of supported projections (dimension of view relative to Tenant denormalized views).
     */
    private Collection<IReadModelProjection> projectionsCatalog;

    /**
     * Tenants store.
     */
    private final EventStore observableTenantsStore;

    /**
     * Projections persistence system.
     */
    private final IDomainRepository<TenantTransactionsCollection> readModelPersistenceRepository;

    /**
     * Provider of Tenant write-model.
     */
    private final IDomainStore<Tenant> tenantsWriteModelStore;

    /**
     * Technical logger.
     */
    private final Logger logger = Logger.getLogger(TenantsReadModelImpl.class.getName());

    /**
     * Default constructor.
     *
     * @param observableStore   Mandatory tenants store observable by projections supported by this read-model.
     * @param managedRepository Mandatory repository allowing management of read-model projection elements.
     * @param tenantsWriteModelStore  Mandatory store of Tenant versions.
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     */
    public TenantsReadModelImpl(EventStore observableStore, IDomainRepository<TenantTransactionsCollection> managedRepository, IDomainStore<Tenant> tenantsWriteModelStore) throws IllegalArgumentException {
        if (observableStore == null) throw new IllegalArgumentException("Observable store parameter is required!");
        this.observableTenantsStore = observableStore;
        if (managedRepository == null) throw new IllegalArgumentException("Managed repository parameter is required!");
        this.readModelPersistenceRepository = managedRepository;
        this.tenantsWriteModelStore = tenantsWriteModelStore;
    }

    /**
     * Get the projections supported by this read-model perimeter.
     *
     * @return A collection of projections optimized for query and providing of denormalized views of tenants.
     */
    @Override
    public Collection<IReadModelProjection> projections() {
        if (projectionsCatalog == null) {
            // Initialize the catalog
            projectionsCatalog = new ArrayList<>();

            // --- SUPPORTED PROJECTIONS CATALOG DEFINITION ---
            // Build each projection implementation class as optimized dimensions supporting this read-model
            projectionsCatalog.add(new TenantLabelOptimizedProjectionImpl(readModelPersistenceRepository, tenantsWriteModelStore));

            // --- PLUG OF WRITE-MODEL WITH READ-MODEL PROJECTIONS MONITORING ---
            // Subscribe the read-model to the write-model about Tenants
            // allowing dispatching of any store's change event to managed projections
            // Subscribe to events notification regarding any tenant
            this.observableTenantsStore.subscribe(new DomainEventSubscriber<DomainEvent>() {
                /**
                 *  Listener of write-model changes, ensuring real-time refresh of read-model projections as read-model views.
                 */
                @Override
                public void handleEvent(DomainEvent event) {
                    try {
                        // Refresh the read-model projections catalog, regarding the new state of Tenant stream

                        // Find last version of the tenant from write model's store (event stream with hydration)

                        // and replace the current version (if exist) of each ReadModel persistence layer (providing querying capabilities) by the new
                        // up-to-date aggregate version according to pre-existing create views projections
                        for (IReadModelProjection projection : projectionsCatalog) {
                            projection.when(event);
                        }
                    } catch (Exception e) {
                        // Log impossible identification of changed information
                        logger.log(Level.SEVERE, e.getMessage(), e);
                    }
                }

                @Override
                public Class<?> subscribeToEventType() {
                    return DomainEvent.class;
                }
            });
        }

        return projectionsCatalog;
    }

    /**
     * Search in projections catalog a projection supporting the contract requested.
     *
     * @param projectionProvided Mandatory contract of projection to find.
     * @return A supported and available projection, or null when not found. Null returned when projectionProvided parameter is not defined.
     */
    @Override
    public IReadModelProjection getProjection(Class<? extends IReadModelProjection> projectionProvided) {
        if (projectionProvided != null) {
            // Search the type of projection implementation class from catalog
            for (IReadModelProjection supported : projections()) {
                if (projectionProvided.isAssignableFrom(supported.getClass())) {
                    return supported;
                }
            }
        }
        return null;
    }
}
