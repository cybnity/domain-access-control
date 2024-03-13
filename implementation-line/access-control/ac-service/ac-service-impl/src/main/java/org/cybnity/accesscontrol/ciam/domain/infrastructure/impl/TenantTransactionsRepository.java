package org.cybnity.accesscontrol.ciam.domain.infrastructure.impl;

import org.cybnity.accesscontrol.domain.service.api.model.TenantTransactionsCollection;
import org.cybnity.framework.domain.ISessionContext;
import org.cybnity.framework.domain.infrastructure.IDomainRepository;
import org.cybnity.framework.domain.model.Repository;
import org.cybnity.framework.immutable.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Implementation repository optimized for query regarding TenantItem versions (e.g projections of optimized search of tenant from label).
 * This store is delegating persistence services to UIAM server (Rest API) via connector.
 */
public class TenantTransactionsRepository extends Repository implements IDomainRepository<TenantTransactionsCollection> {

    private static TenantTransactionsRepository singleton;

    /**
     * Repository technical logger.
     */
    private final Logger logger = Logger.getLogger(TenantTransactionsRepository.class.getName());

    /**
     * Reserved constructor.
     */
    private TenantTransactionsRepository() {
        super();
    }

    /**
     * Get a repository instance.
     *
     * @return A singleton instance.
     */
    public static TenantTransactionsRepository instance() {
        if (singleton == null) {
            // Initializes singleton instance
            singleton = new TenantTransactionsRepository();
        }
        return singleton;
    }

    @Override
    public TenantTransactionsCollection nextIdentity(ISessionContext ctx) {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        return null;
    }

    @Override
    public TenantTransactionsCollection factOfId(Identifier identifier, ISessionContext ctx) {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        return null;
    }

    @Override
    public List<TenantTransactionsCollection> queryWhere(Map<String, String> queryParameters, ISessionContext ctx) {
        if (queryParameters!=null) {
            // Read supported parameter applicable as filter on collection to return

        }
        return null;
    }

    @Override
    public boolean remove(TenantTransactionsCollection tenant, ISessionContext ctx) {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        return false;
    }

    @Override
    public void removeAll(Collection<TenantTransactionsCollection> collection, ISessionContext ctx) {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
    }

    @Override
    public TenantTransactionsCollection save(TenantTransactionsCollection tenant, ISessionContext ctx) {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        return null;
    }

    @Override
    public void saveAll(Collection<TenantTransactionsCollection> collection, ISessionContext ctx) {
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

}
