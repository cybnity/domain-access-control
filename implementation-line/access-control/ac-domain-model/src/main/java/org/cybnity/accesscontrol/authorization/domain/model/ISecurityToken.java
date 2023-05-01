package org.cybnity.accesscontrol.authorization.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Represents an OAuth2 token.
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public interface ISecurityToken {

    /**
     * Get base64 formalized value of this token.
     * 
     * @return A character chain coded in base64 format.
     */
    public String base64TokenValue();

    /**
     * Get plain text value of this token.
     * 
     * @return A character chain.
     */
    public String plainTextTokenValue();

    /**
     * Get the private key used to sign this token.
     * 
     * @return A private key.
     */
    public String signaturePrivateKey();

    /**
     * Get hash representing this token.
     * 
     * @return Hashed version of this token.
     */
    public int hashCode();

    @Override
    boolean equals(Object obj);
}
