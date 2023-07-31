package org.cybnity.accesscontrol.iam.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Attribute that represent a scope of authorization.
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public interface ScopeAttribute {

    /**
     * Get the logical name of this scope.
     * 
     * @return A name.
     */
    public String name();
}
