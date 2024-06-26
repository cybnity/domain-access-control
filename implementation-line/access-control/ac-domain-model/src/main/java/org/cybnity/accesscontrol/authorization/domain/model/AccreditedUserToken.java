package org.cybnity.accesscontrol.authorization.domain.model;

import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

import java.util.Collection;

/**
 * Represents an accredited JWT token allowed to an authenticated subject.
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public class AccreditedUserToken {

    /**
     * Default constructor.
     * 
     * @param tenant            Mandatory tenant.
     * @param userIdentityId    Mandatory user identity identifier.
     * @param userAccountId     Mandatory user account identifier.
     * @param originalToken     Mandatory token.
     * @param userClaims        Optional claims.
     * @param userHabilitations Optional habilitations.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public AccreditedUserToken(Tenant tenant, String userIdentityId, String userAccountId, JWTToken originalToken,
	    Collection<IClaim> userClaims, Collection<IAccreditation> userHabilitations)
	    throws IllegalArgumentException {
	if (originalToken == null)
	    throw new IllegalArgumentException("Mandatory original token is missing!");
	if (userAccountId == null || "".equals(userAccountId))
	    throw new IllegalArgumentException("The user account id parameter is required!");
	if (userIdentityId == null || "".equals(userIdentityId))
	    throw new IllegalArgumentException("The user identity id parameter is required!");
	if (tenant == null)
	    throw new IllegalArgumentException("The tenant parameter is required!");

    }
}
