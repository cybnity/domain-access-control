package org.cybnity.accesscontrol.authorization.domain.model;

import org.cybnity.framework.immutable.EntityReference;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

import java.time.OffsetDateTime;

/**
 * Represents an access token allowed to an account.
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public interface IAccessToken extends ISecurityToken {

    /**
     * Get the authorization that was source of this token allocation.
     * 
     * @return An authorization.
     */
    IAuthorization authorizedBy();

    /**
     * Get the expiration time regarding this authorized token usage.
     * 
     * @return A time.
     */
    OffsetDateTime expireAt();

    /**
     * Identify if this token have been signed.
     * 
     * @return True if signed token. Else return false.
     */
    boolean isSigned();

    /**
     * Get reference of the tenant which is a scope of usage regarding this
     * authorized token.
     * 
     * @return A tenant reference.
     */
    EntityReference tenant();

    /**
     * Get the type name of this access token.
     * 
     * @return A type name of this access token.
     */
    String type();
}
