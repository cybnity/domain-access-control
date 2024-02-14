package org.cybnity.accesscontrol.iam.domain.model;

import org.cybnity.framework.domain.IReadModel;
import org.cybnity.framework.immutable.Identifier;

/**
 * Represents a read-oriented and query optimized repository (also sometimes called Aggregate
 * store, or Aggregate-Oriented database) contract for the Account bounded context.
 *
 * @author olivier
 */
public interface AccountsReadModel extends IReadModel {

    /**
     * How many user accounts are existing in the scope of an identified tenant.
     *
     * @param tenantIdentifier        Mandatory identifier of the tenant scope which shall be considered for user accounts search.
     * @param withMailAddressInStatus Optional state of mail address attached to the account evaluated.
     * @return A count of accounts. Null is unknown.
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     */
    public Integer accountsCount(Identifier tenantIdentifier, MailAddress.Status withMailAddressInStatus) throws IllegalArgumentException;
}
