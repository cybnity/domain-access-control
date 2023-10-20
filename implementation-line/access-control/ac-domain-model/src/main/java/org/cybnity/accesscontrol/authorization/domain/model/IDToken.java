package org.cybnity.accesscontrol.authorization.domain.model;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Represents a token based on OIDC protocol, as standardized JWT token by
 * OpenID core fields.
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public abstract class IDToken implements ISecurityToken {

    protected OffsetDateTime expiration;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime authentifiedAt;
    private final Collection<IClaim> userClaims;
    private final Collection<IAccreditation> userHabilitations;

    /**
     * Default constructor.
     * 
     * @param userClaims        Optional claims.
     * @param userHabilitations Optional accreditations.
     * @param expiration        Optional date of expiration of this token.
     * @param authentifiedAt    Optional date of owern's authentication.
     */
    public IDToken(Collection<IClaim> userClaims, Collection<IAccreditation> userHabilitations,
	    OffsetDateTime expiration, OffsetDateTime authentifiedAt) {
	// Create immutable time
	this.createdAt = OffsetDateTime.now();
	this.expiration = expiration;
	this.authentifiedAt = authentifiedAt;
	this.userClaims = userClaims;
	this.userHabilitations = userHabilitations;
    }

    /**
     * Get the user accreditations when known.
     * 
     * @return A collection of user's accreditations or null.
     */
    public Collection<IAccreditation> userHabilitations() {
	if (this.userHabilitations != null)
	    return List.copyOf(this.userHabilitations);
	return null;
    }

    /**
     * Get the user claims when known.
     * 
     * @return A collection of user's claims or null.
     */
    public Collection<IClaim> userClaims() {
	if (this.userClaims != null)
	    return List.copyOf(this.userClaims);
	return null;
    }

    /**
     * Check if an access token if usable regarding the user habilitations.
     * 
     * @param access To check. Return false if parameter is null.
     * @return True if usable according to the user's habilitations. Else false when
     *         not authorized. False by default.
     */
    public abstract boolean isValid(IAccessToken access);

    /**
     * Get the time when the token owner was authentified.
     * 
     * @return A date or null.
     */
    public OffsetDateTime authentifiedAt() {
	return this.authentifiedAt;
    }

    /**
     * Get expiration time of this token.
     * 
     * @return A date or null if unknown.
     */
    public OffsetDateTime expireAt() {
	return this.expiration;
    }

    /**
     * Get the date when this token was created.
     * 
     * @return A date of token instantiation.
     */
    public OffsetDateTime createdAt() {
	return this.createdAt;
    }
}
