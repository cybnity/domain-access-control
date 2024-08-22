package org.cybnity.accesscontrol.iam.domain.infrastructure.impl;

import org.cybnity.accesscontrol.domain.infrastructure.impl.projections.AccessControlDomainGraphImpl;
import org.cybnity.accesscontrol.domain.infrastructure.impl.projections.AccessControlDomainReadModelImpl;
import org.cybnity.accesscontrol.iam.domain.model.Account;
import org.cybnity.application.accesscontrol.ui.api.AccessControlDomainModel;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.ISessionContext;
import org.cybnity.framework.domain.infrastructure.IDomainRepository;
import org.cybnity.framework.domain.infrastructure.IDomainStore;
import org.cybnity.framework.domain.model.IDomainModel;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.infrastructure.technical.registry.repository.impl.janusgraph.AbstractReadModelRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Implementation repository optimized for query regarding Account objects.
 * This store is delegating persistence services to UIAM server via connector.
 */
public class AccountsRepository extends AbstractReadModelRepository implements IDomainRepository<Account> {

    private static AccountsRepository SINGLETON;

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
    private AccountsRepository(IContext ctx, IDomainStore<Tenant> tenantsWriteModelStore) throws UnoperationalStateException, IllegalArgumentException {
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
    public static AccountsRepository instance(IContext ctx, IDomainStore<Tenant> tenantsWriteModelStore) throws UnoperationalStateException, IllegalArgumentException {
        if (SINGLETON == null) {
            // Initializes singleton instance
            SINGLETON = new AccountsRepository(ctx, tenantsWriteModelStore);
        }
        return SINGLETON;
    }

    @Override
    public void freeResources() {

    }

    @Override
    public Account nextIdentity(ISessionContext iSessionContext) {
        return null;
    }

    @Override
    public Account factOfId(Identifier identifier, ISessionContext iSessionContext) {
        return null;
    }

    @Override
    public boolean remove(Account account, ISessionContext iSessionContext) {
        return false;
    }

    @Override
    public void removeAll(Collection<Account> collection, ISessionContext iSessionContext) {

    }

    @Override
    public Account save(Account account, ISessionContext iSessionContext) {
        return null;
    }

    @Override
    public void saveAll(Collection<Account> collection, ISessionContext iSessionContext) {

    }

    @Override
    public List<Account> queryWhere(Map<String, String> map, ISessionContext iSessionContext) {
        return null;
    }

    @Override
    public String queryNameBasedOn() {
        return Command.TYPE;
    }

    @Override
    public Account nextIdentity() {
        return null;
    }

    @Override
    public Account factOfId(Identifier identifier) {
        return null;
    }

    @Override
    public boolean remove(Account account) {
        return false;
    }

    @Override
    public void removeAll(Collection<Account> collection) {

    }

    @Override
    public Account save(Account account) {
        return null;
    }

    @Override
    public void saveAll(Collection<Account> collection) {

    }

}
