package org.cybnity.accesscontrol.authorization.domain.model;

import java.util.Collection;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Represents an OAuth2 JWT Token as exchange format.
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public class JWTToken {

    private IDToken token;

    /**
     * Default constructor.
     * 
     * @param token Optional original token.
     */
    public JWTToken(IDToken token) {
	this.token = token;
    }

    /**
     * Get the user claims which is owner of this token.
     * 
     * @return Claims or null.
     */
    public Collection<IClaim> userClaims() {
	if (this.token != null)
	    return this.token.userClaims();
	return null;
    }

    /**
     * Get the user habiliations allowed to the owner of this token.
     * 
     * @return Current allowed accreditations or null.
     */
    public Collection<IAccreditation> userHabilitations() {
	if (this.token != null)
	    return this.token.userHabilitations();
	return null;
    }

}
