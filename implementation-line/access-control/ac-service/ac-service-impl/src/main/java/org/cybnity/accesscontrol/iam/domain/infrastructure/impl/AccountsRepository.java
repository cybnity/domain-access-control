package org.cybnity.accesscontrol.iam.domain.infrastructure.impl;

import org.cybnity.accesscontrol.iam.domain.model.Account;
import org.cybnity.accesscontrol.iam.domain.model.AccountsReadModel;
import org.cybnity.accesscontrol.iam.domain.model.MailAddress;
import org.cybnity.framework.domain.ISessionContext;
import org.cybnity.framework.domain.infrastructure.IDomainRepository;
import org.cybnity.framework.immutable.Identifier;

import java.util.Collection;

/**
 * Implementation repository optimized for query regarding Account objects.
 * This store is delegating persistence services to UIAM server via connector.
 */
public class AccountsRepository implements IDomainRepository<Account>, AccountsReadModel {

    private static AccountsRepository singleton;

    /**
     * Reserved constructor.
     */
    private AccountsRepository() {
    }

    /**
     * Get a repository instance.
     *
     * @return A singleton instance.
     */
    public static AccountsReadModel getInstance() {
        if (singleton == null) {
            // Initializes singleton instance
            singleton = new AccountsRepository();
        }
        return singleton;
    }

    @Override
    public Integer accountsCount(Identifier tenantIdentifier, MailAddress.Status withMailAddressInStatus) throws IllegalArgumentException {
        return null;
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
