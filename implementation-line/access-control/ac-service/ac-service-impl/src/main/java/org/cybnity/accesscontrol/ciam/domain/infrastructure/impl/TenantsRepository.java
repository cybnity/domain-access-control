package org.cybnity.accesscontrol.ciam.domain.infrastructure.impl;

import org.cybnity.accesscontrol.ciam.domain.model.TenantsReadModel;
import org.cybnity.accesscontrol.iam.domain.model.IdentitiesReadModel;
import org.cybnity.framework.domain.ISessionContext;
import org.cybnity.framework.domain.infrastructure.IDomainRepository;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.Identifier;

import java.util.Collection;

/**
 * Implementation repository optimized for query regarding Tenant objects.
 * This store is delegating persistence services to UIAM server (Rest API) via connector.
 */
public class TenantsRepository implements IDomainRepository<Tenant>, TenantsReadModel {

    private static TenantsRepository singleton;

    /**
     * Optional custom identities repository usable in place of default Identity server configured into the User Access Management system.
     */
    private IdentitiesReadModel identitiesRepository;

    /**
     * Reserved constructor.
     *
     * @param identitiesRepository Optional repository of identities which can be used in place of default configuration managed by the Access Management server.
     */
    private TenantsRepository(IdentitiesReadModel identitiesRepository) {
        this.identitiesRepository = identitiesRepository;
    }

    /**
     * Reserved constructor.
     */
    private TenantsRepository() {
    }

    /**
     * Get a repository instance.
     *
     * @param identitiesRepository Optional repository of identities which can be used in place of default configuration managed by the Access Management server.
     * @return A singleton instance.
     */
    public static TenantsReadModel getInstance(IdentitiesReadModel identitiesRepository) {
        if (singleton == null) {
            // Initializes singleton instance
            singleton = new TenantsRepository(identitiesRepository);
        }
        return singleton;
    }

    /**
     * Get a repository instance.
     *
     * @return A singleton instance.
     */
    public static TenantsReadModel getInstance() {
        if (singleton == null) {
            // Initializes singleton instance
            singleton = new TenantsRepository();
        }
        return singleton;
    }

    @Override
    public Tenant nextIdentity(ISessionContext iSessionContext) {
        return null;
    }

    @Override
    public Tenant factOfId(Identifier identifier, ISessionContext iSessionContext) {
        return null;
    }

    @Override
    public boolean remove(Tenant tenant, ISessionContext iSessionContext) {
        return false;
    }

    @Override
    public void removeAll(Collection<Tenant> collection, ISessionContext iSessionContext) {

    }

    @Override
    public Tenant save(Tenant tenant, ISessionContext iSessionContext) {
        return null;
    }

    @Override
    public void saveAll(Collection<Tenant> collection, ISessionContext iSessionContext) {

    }

    @Override
    public Tenant nextIdentity() {
        return null;
    }

    @Override
    public Tenant factOfId(Identifier identifier) {
        return null;
    }

    @Override
    public boolean remove(Tenant tenant) {
        return false;
    }

    @Override
    public void removeAll(Collection<Tenant> collection) {

    }

    @Override
    public Tenant save(Tenant tenant) {
        return null;
    }

    @Override
    public void saveAll(Collection<Tenant> collection) {

    }

}
