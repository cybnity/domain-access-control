package org.cybnity.accesscontrol.domain.infrastructure.impl;

import org.cybnity.accesscontrol.domain.infrastructure.impl.projections.AccessControlDomainGraphImpl;
import org.cybnity.accesscontrol.domain.infrastructure.impl.projections.AccessControlDomainReadModelImpl;
import org.cybnity.application.accesscontrol.translator.ui.api.AccessControlDomainModel;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.infrastructure.IDomainRepository;
import org.cybnity.framework.domain.infrastructure.IDomainStore;
import org.cybnity.framework.domain.model.IDomainModel;
import org.cybnity.framework.domain.model.SocialEntity;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.infrastructure.technical.registry.repository.impl.janusgraph.AbstractReadModelRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Implementation repository optimized for query regarding Identity objects.
 * This store is delegating persistence services to Identity server via connector (e.g identity server used by Keycloak UIAM).
 */
public class IdentitiesRepository extends AbstractReadModelRepository implements IDomainRepository<SocialEntity> {

    private static IdentitiesRepository SINGLETON;

    /**
     * Configuration of the domain which is responsible for the data-views perimeter managed over this repository.
     */
    private static final IDomainModel READ_MODEL_OWNERSHIP = new AccessControlDomainModel();

    /**
     * Reserved constructor that initialize the graph instance under responsibility of this repository, with preparation of its read-model scope (set of projections supported).
     *
     * @param ctx                    Mandatory context.
     * @param tenantsWriteModelStore Mandatory rehydration responsible for tenant domain objects.
     * @throws UnoperationalStateException When impossible connection and initialization of the graph model manipulated by this repository.
     * @throws IllegalArgumentException    When mandatory parameter is missing.
     */
    private IdentitiesRepository(IContext ctx, IDomainStore<Tenant> tenantsWriteModelStore) throws UnoperationalStateException, IllegalArgumentException {
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
    public static IdentitiesRepository instance(IContext ctx, IDomainStore<Tenant> tenantsWriteModelStore) throws UnoperationalStateException, IllegalArgumentException {
        if (SINGLETON == null) {
            // Initializes singleton instance
            SINGLETON = new IdentitiesRepository(ctx, tenantsWriteModelStore);
        }
        return SINGLETON;
    }

    @Override
    public void freeUpResources() {

    }

    @Override
    public SocialEntity nextIdentity(IContext ctx) {
        return null;
    }

    @Override
    public SocialEntity factOfId(Identifier identifier, IContext ctx) {
        return null;
    }

    @Override
    public boolean remove(SocialEntity socialEntity, IContext ctx) {
        return false;
    }

    @Override
    public void removeAll(Collection<SocialEntity> collection, IContext ctx) {

    }

    @Override
    public SocialEntity save(SocialEntity socialEntity, IContext ctx) {
        return null;
    }

    @Override
    public void saveAll(Collection<SocialEntity> collection,IContext ctx) {

    }

    @Override
    public List<SocialEntity> queryWhere(Map<String, String> map, IContext iSessionContext) {
        return null;
    }

    @Override
    public String queryNameBasedOn() {
        return Command.TYPE;
    }

    @Override
    public SocialEntity nextIdentity() {
        return null;
    }

    @Override
    public SocialEntity factOfId(Identifier identifier) {
        return null;
    }

    @Override
    public boolean remove(SocialEntity socialEntity) {
        return false;
    }

    @Override
    public void removeAll(Collection<SocialEntity> collection) {

    }

    @Override
    public SocialEntity save(SocialEntity socialEntity) {
        return null;
    }

    @Override
    public void saveAll(Collection<SocialEntity> collection) {

    }

}
