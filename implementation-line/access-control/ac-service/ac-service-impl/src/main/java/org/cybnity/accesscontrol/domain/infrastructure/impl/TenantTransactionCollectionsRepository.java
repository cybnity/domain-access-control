package org.cybnity.accesscontrol.domain.infrastructure.impl;

import org.cybnity.accesscontrol.domain.infrastructure.impl.projections.AccessControlDomainGraphImpl;
import org.cybnity.accesscontrol.domain.infrastructure.impl.projections.AccessControlDomainReadModelImpl;
import org.cybnity.accesscontrol.domain.service.api.event.ACApplicationQueryName;
import org.cybnity.accesscontrol.domain.service.api.model.TenantDataView;
import org.cybnity.accesscontrol.domain.service.api.model.TenantTransactionsCollection;
import org.cybnity.application.accesscontrol.translator.ui.api.AccessControlDomainModel;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.*;
import org.cybnity.framework.domain.event.IEventType;
import org.cybnity.framework.domain.event.QueryFactory;
import org.cybnity.framework.domain.infrastructure.IDomainStore;
import org.cybnity.framework.domain.model.DomainEntity;
import org.cybnity.framework.domain.model.IDomainModel;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.infrastructure.technical.registry.repository.impl.janusgraph.AbstractReadModelRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Implementation repository optimized for query regarding TenantItem versions (e.g projections of optimized search of tenant from label).
 * This repository is delegating persistence services to GraphQL or caching database optimized for read-model access and query.
 */
public class TenantTransactionCollectionsRepository extends AbstractReadModelRepository implements ITenantTransactionsCollectionRepository {

    private static TenantTransactionCollectionsRepository SINGLETON;

    /**
     * Configuration of the domain which is responsible for the data-views perimeter managed over this repository.
     */
    private static final IDomainModel READ_MODEL_OWNERSHIP = new AccessControlDomainModel();

    /**
     * Repository technical logger.
     */
    private final Logger logger = Logger.getLogger(TenantTransactionCollectionsRepository.class.getName());

    /**
     * Reserved constructor that initialize the graph instance under responsibility of this repository, with preparation of its read-model scope (set of projections supported).
     *
     * @param ctx                    Mandatory context.
     * @param tenantsWriteModelStore Mandatory rehydration responsible for tenant domain objects.
     * @throws UnoperationalStateException When impossible connection and initialization of the graph model manipulated by this repository.
     * @throws IllegalArgumentException    When mandatory parameter is missing.
     */
    private TenantTransactionCollectionsRepository(IContext ctx, IDomainStore<Tenant> tenantsWriteModelStore) throws UnoperationalStateException, IllegalArgumentException {
        super(new AccessControlDomainGraphImpl(ctx));
        // Define set of projections identifying the read-model scope that can manipulate the graph
        this.setManagedProjections(new AccessControlDomainReadModelImpl(ctx, new AccessControlDomainGraphImpl(ctx), READ_MODEL_OWNERSHIP, this, tenantsWriteModelStore));
    }

    /**
     * Get a repository instance.
     *
     * @param tenantsWriteModelStore Mandatory rehydration responsible for tenant domain objects.
     * @return A singleton instance.
     * @throws UnoperationalStateException When impossible connection and initialization of the graph model manipulated by this repository.
     * @throws IllegalArgumentException    When mandatory parameter is missing.
     */
    public static TenantTransactionCollectionsRepository instance(IContext ctx, IDomainStore<Tenant> tenantsWriteModelStore) throws UnoperationalStateException, IllegalArgumentException {
        if (SINGLETON == null) {
            // Initializes singleton instance
            SINGLETON = new TenantTransactionCollectionsRepository(ctx, tenantsWriteModelStore);
        }
        return SINGLETON;
    }

    /**
     * Close the graph model.
     */
    @Override
    public void freeUpResources() {
        this.graphModel().freeUpResources();
    }

    @Override
    public String queryNameBasedOn() {
        return Command.TYPE;
    }

    @Override
    public List<TenantTransactionsCollection> queryWhere(Map<String, String> searchCriteria, IContext ctx) throws IllegalArgumentException, UnsupportedOperationException, UnoperationalStateException {
        if (searchCriteria != null) {
            // Identify the query name based on query type (projection that support the query parameters and specific data path/structure)
            String queryName = searchCriteria.get(queryNameBasedOn());
            if (queryName != null && !queryName.isEmpty()) {
                // Identify query event type from domain's referential of queries supported
                IEventType queryType = Enum.valueOf(/* Referential catalog of query types supported by the repository domain */ ACApplicationQueryName.class, queryName);

                // Search a projection that is declared supporting the requested query type
                final IReadModelProjection managedProjection = this.findBySupportedQuery(queryType);
                if (managedProjection != null) {
                    // Prepare instance of query command event to submit on found projection
                    final Command queryToPerform = QueryFactory.create(/* Name of query type */ queryName, /* query command UUID */
                            new DomainEntity(IdentifierStringBased.generate(null)),
                            /* Prepare query command attributes set based on search criteria submitted */ this.prepareQueryParameters(searchCriteria),
                            /* None prior command managed during this explicit query call */ null);

                    // Execute the query via delegation to the found projection (owner of data structure and supported parameter types)
                    // See https://www.callicoder.com/java-8-completablefuture-tutorial/ for help and possible implementation approaches for sync/async execution of query
                    CompletableFuture<Optional<DataTransferObject>> executionResulting = CompletableFuture.supplyAsync(() -> {
                        // Execute the query onto the projection and deliver the optional results
                        try {
                            return managedProjection.when(queryToPerform);
                        } catch (UnoperationalStateException e) {
                            throw new RuntimeException(e);
                        }
                    }).thenApply(IQueryResponse::value);

                    try {
                        // Read results when available via CompletableFuture.get() blocking method invoked on IQueryResponse,
                        // that waits until the Future is completed (return result after its completion)
                        Optional<DataTransferObject> dto = executionResulting.get();
                        if (dto.isPresent()) {
                            // Build domain data view results to return
                            List<TenantTransactionsCollection> results;
                            DataTransferObject resultProvider = dto.get();
                            if (TenantDataView.class.isAssignableFrom(resultProvider.getClass())) {
                                // Valid type of collected data view object managed by this repository
                                // that can be returned as unique result
                                results = new LinkedList<>();
                                TenantDataView record = (TenantDataView) resultProvider;
                                TenantTransactionsCollection col = new TenantTransactionsCollection(record.valueOfProperty(TenantDataView.PropertyAttributeKey.IDENTIFIED_BY));
                                col.add(record);
                                results.add(col);
                                return results;
                            } else if (TenantTransactionsCollection.class.isAssignableFrom(resultProvider.getClass())) {
                                // Valid type of collected data view object managed by this repository
                                // that can be returned as unique result
                                results = new LinkedList<>();
                                results.add((TenantTransactionsCollection) resultProvider);
                                return results;
                            }
                        }
                        return null; // Confirm that none results are provided from the executed query
                    } catch (Exception e) {
                        throw new UnoperationalStateException(e);
                    }
                } else {
                    // else unknown query name or not supported by the read-model under responsibility of this repository,
                    // which make impossible to perform the query with potential result finding
                    throw new UnsupportedOperationException("The requested query is not supported by any projection of this repository. Only ACApplicationQueryName values are supported as query name!");
                }
            } else {
                // else unknown query name to search on repository
                throw new IllegalArgumentException("A defined query name is required from search criteria (Command.TYPE criteria with defined value)!");
            }
        }
        throw new IllegalArgumentException("Search criteria parameter is required!");
    }

    @Override
    public TenantTransactionsCollection nextIdentity(IContext ctx) {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        return null;
    }

    @Override
    public TenantTransactionsCollection factOfId(Identifier identifier, IContext ctx) {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        return null;
    }

    @Override
    public boolean remove(TenantTransactionsCollection tenant, IContext ctx) {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        return false;
    }

    @Override
    public void removeAll(Collection<TenantTransactionsCollection> collection, IContext ctx) {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
    }

    @Override
    public TenantTransactionsCollection save(TenantTransactionsCollection tenant, IContext ctx) {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        return null;
    }

    @Override
    public void saveAll(Collection<TenantTransactionsCollection> collection, IContext ctx) {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
    }

    @Override
    public TenantTransactionsCollection nextIdentity() {
        return null;
    }

    @Override
    public TenantTransactionsCollection factOfId(Identifier identifier) {
        return null;
    }

    @Override
    public boolean remove(TenantTransactionsCollection tenant) {
        return false;
    }

    @Override
    public void removeAll(Collection<TenantTransactionsCollection> collection) {

    }

    @Override
    public TenantTransactionsCollection save(TenantTransactionsCollection tenant) {
        return null;
    }

    @Override
    public void saveAll(Collection<TenantTransactionsCollection> collection) {

    }

    /**
     * Drop graph model schema and data.
     *
     * @throws UnoperationalStateException When problem during graph model drop.
     */
    @Override
    public void drop() throws UnoperationalStateException {
        this.graphModel().drop();
    }
}
