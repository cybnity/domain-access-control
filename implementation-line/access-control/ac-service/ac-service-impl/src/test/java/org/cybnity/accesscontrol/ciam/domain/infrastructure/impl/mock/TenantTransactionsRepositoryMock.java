package org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.mock;

import org.cybnity.accesscontrol.domain.service.api.model.TenantTransaction;
import org.cybnity.accesscontrol.domain.service.api.model.TenantTransactionsCollection;
import org.cybnity.framework.domain.ISessionContext;
import org.cybnity.framework.domain.infrastructure.IDomainRepository;
import org.cybnity.framework.domain.model.Repository;
import org.cybnity.framework.immutable.Identifier;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Mocked repository implementation class reusing a common in-memory store impl.
 */
public class TenantTransactionsRepositoryMock extends Repository implements IDomainRepository<TenantTransactionsCollection> {

    private static TenantTransactionsRepositoryMock singleton;
    private final Logger logger = Logger.getLogger(TenantTransactionsRepositoryMock.class.getName());

    /**
     * In-Memory map of identified tenants with lifecycle dimension.
     */
    private final Map</*tenant id*/ String, /* tenant lifecycle history*/ TenantTransactionsCollection> catalogOfIdentifiedCollections = new HashMap<>();

    /**
     * Reserved constructor.
     */
    private TenantTransactionsRepositoryMock() {
        super();
    }

    /**
     * Get a repository instance.
     *
     * @return A singleton instance.
     */
    public static TenantTransactionsRepositoryMock instance() {
        if (singleton ==null) {
            singleton = new TenantTransactionsRepositoryMock();
        }
        return singleton;
    }

    /**
     * Accessor allowing direct read of repository implementation during unit test execution.
     *
     * @return Store's container instance.
     */
    public Map</*tenant id*/ String, /* tenant lifecycle history*/ TenantTransactionsCollection> catalogOfIdentifiedCollections() {
        return catalogOfIdentifiedCollections;
    }

    @Override
    public void freeResources() {

    }

    @Override
    public TenantTransactionsCollection nextIdentity(ISessionContext ctx) {
        return null;
    }

    @Override
    public TenantTransactionsCollection factOfId(Identifier identifier, ISessionContext ctx) {
        return factOfId(identifier);
    }

    @Override
    public TenantTransactionsCollection factOfId(Identifier identifier) {
        if (identifier != null) {
            return catalogOfIdentifiedCollections.get(identifier.value().toString());
        }
        return null;
    }


    @Override
    public List<TenantTransactionsCollection> queryWhere(Map<String, String> filteringParameters, ISessionContext ctx) {
        // Get all existing collections without any filtering criteria
        List<TenantTransactionsCollection> reducedResults = new LinkedList<>(catalogOfIdentifiedCollections.values());

        if (filteringParameters != null) {
            // Read occurrence date filtering criteria when defined
            String tenantOccurrenceDateFilter = filteringParameters.get(TenantTransactionsCollection.QueryParameter.TENANT_VERSION_DATE.name());

            // Read activity state filtering criteria when defined
            String tenantActivityStatusFilter = filteringParameters.get(TenantTransactionsCollection.QueryParameter.TENANT_ACTIVITY_STATUS.name());
            Boolean activityStatusCondition = null;
            if (tenantActivityStatusFilter != null && !tenantActivityStatusFilter.isEmpty()) {
                activityStatusCondition = Boolean.valueOf(tenantActivityStatusFilter);
            }

            // Read tenant identifier filtering criteria when defined
            String tenantIdFilter = filteringParameters.get(TenantTransactionsCollection.QueryParameter.TENANT_IDENTIFIER.name());
            if (tenantIdFilter != null && !tenantIdFilter.isEmpty()) {
                TenantTransactionsCollection identifiedCollection = catalogOfIdentifiedCollections.get(tenantIdFilter);
                if (identifiedCollection != null) {
                    List<TenantTransactionsCollection> tenantIdFilteredResults = new LinkedList<>();
                    tenantIdFilteredResults.add(identifiedCollection);
                    reducedResults = tenantIdFilteredResults; // Assign as reduced results by tenant identifier
                }
            }

            // Read label filtering criteria when defined
            String labelFilter = filteringParameters.get(TenantTransactionsCollection.QueryParameter.TENANT_LABEL.name());

            // Search tenant with equals label
            if (labelFilter != null && !labelFilter.isEmpty()) {
                // Search collection from tenant label
                List<TenantTransaction> haveBeenNamedDuringItsHistory = new ArrayList<>();
                for (TenantTransactionsCollection collection : reducedResults) {
                    // Read the collection latest name
                    List<TenantTransaction> versions = collection.versions();
                    for (TenantTransaction tenant : versions) {
                        if (tenant.label.equals(labelFilter)) {
                            // Eligible to name owning during its lifecycle history (had be named like this during its life)
                            haveBeenNamedDuringItsHistory.add(tenant);
                        }
                    }
                }
                // Find the latest label assigned to an owner (tenant)
                // Find from eligible tenants latest version which is currently named as the searched label
                haveBeenNamedDuringItsHistory.sort(Comparator.comparing(o -> o.versionedAt));

                // Retain the last owner
                if (!haveBeenNamedDuringItsHistory.isEmpty()) {
                    List<TenantTransactionsCollection> labelFilteredResults = new LinkedList<>();
                    for (int i = haveBeenNamedDuringItsHistory.size() - 1; i >= 0; i--) {
                        TenantTransaction trans = haveBeenNamedDuringItsHistory.get(i);
                        // Identify and get the collection which is parent of it
                        TenantTransactionsCollection identifiedCollection = catalogOfIdentifiedCollections.get(trans.identifiedBy);

                        // RULE : operation status filtering condition
                        if (activityStatusCondition != null) {
                            // Select only instance which is in equals operable status
                            if (trans.activityStatus != null) {
                                // Check if known status equals to filtering criteria regarding operational state
                                if (trans.activityStatus.equals(activityStatusCondition)) {
                                    // It's the last one who have been owner of the name, AND that is in operational activity status
                                    // So retain it as latest version
                                    if (identifiedCollection != null) {
                                        labelFilteredResults.add(identifiedCollection);
                                        break; // Stop read of eligible old owners
                                    }
                                }
                            }
                        } else {
                            // None filtering about operational status
                            // So retain the last one as ordered
                            labelFilteredResults.add(identifiedCollection);
                            break; // Stop read of eligible old owners
                        }
                    }
                    if (!labelFilteredResults.isEmpty()) {
                        // Retain only filtered results regarding label, as reduced results
                        reducedResults = labelFilteredResults;
                    } else {
                        // None equals label tenant have been found
                        reducedResults.clear();// Remove all previous found results where none have equals tenant naming label
                    }
                } else {
                    // None equals label tenant have been found
                    reducedResults.clear();// Remove all previous found results where none have equals tenant naming label
                }
            }
        }
        return reducedResults;
    }

    @Override
    public boolean remove(TenantTransactionsCollection tenantTransactionsCollection, ISessionContext ctx) {
        return false;
    }

    @Override
    public void removeAll(Collection<TenantTransactionsCollection> collection, ISessionContext ctx) {

    }

    @Override
    public void saveAll(Collection<TenantTransactionsCollection> collection, ISessionContext ctx) {
        this.saveAll(collection);
    }

    @Override
    public TenantTransactionsCollection nextIdentity() {
        return null;
    }

    @Override
    public boolean remove(TenantTransactionsCollection tenantTransactionsCollection) {
        return false;
    }

    @Override
    public void removeAll(Collection<TenantTransactionsCollection> collection) {

    }


    @Override
    public TenantTransactionsCollection save(TenantTransactionsCollection
                                                     tenantTransactionsCollection, ISessionContext ctx) {
        return this.save(tenantTransactionsCollection);
    }

    @Override
    public TenantTransactionsCollection save(TenantTransactionsCollection tenantTransactionsCollection) {
        if (tenantTransactionsCollection != null) {
            // Check if existing
            TenantTransactionsCollection tenantLifeDimension = catalogOfIdentifiedCollections.get(tenantTransactionsCollection.tenantIdentifier());
            if (tenantLifeDimension != null) {
                // Update life with new version
                List<TenantTransaction> copy = new LinkedList<>(tenantTransactionsCollection.versions()); // Thread-safe read of the loop to prevent unexpected results during dimension modification
                AtomicBoolean isAddedVersions = new AtomicBoolean(false);
                copy.forEach((versionsToHistorize) -> {
                    // Add lifecycle version in existing history
                    tenantLifeDimension.add(versionsToHistorize.label, versionsToHistorize.activityStatus, versionsToHistorize.versionedAt);
                    isAddedVersions.set(true);
                });
                if (isAddedVersions.getAcquire()) {
                    // Save upgraded dimension record for tenant
                    catalogOfIdentifiedCollections.put(tenantTransactionsCollection.tenantIdentifier(), tenantLifeDimension);
                }
                return tenantLifeDimension; // Return updated version (including previous history and new added versions)
            } else {
                // Add new dimension record for tenant
                catalogOfIdentifiedCollections.put(tenantTransactionsCollection.tenantIdentifier(), tenantTransactionsCollection);
                return tenantTransactionsCollection;
            }
        }
        return null;
    }

    @Override
    public void saveAll(Collection<TenantTransactionsCollection> collection) {
        if (collection != null) {
            for (TenantTransactionsCollection tenantCollection : collection) {
                this.save(tenantCollection);
            }
        }
    }
}
