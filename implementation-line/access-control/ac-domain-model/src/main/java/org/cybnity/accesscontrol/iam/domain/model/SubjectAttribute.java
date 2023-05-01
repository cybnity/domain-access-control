package org.cybnity.accesscontrol.iam.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Attribute describing a subject who is demanding access (e.r role, group
 * membership, competency, user id...).
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public abstract class SubjectAttribute {

    /**
     * Dedicated scope regarding this attribute.
     */
    private ScopeAttribute scope;

    /**
     * Default constructor based on a scope.
     * 
     * @param scope Mandatory scope of this subject description criteria.
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     */
    public SubjectAttribute(ScopeAttribute scope) throws IllegalArgumentException {
	if (scope == null)
	    throw new IllegalArgumentException("Mandatory scope parameter shall be defined!");
	this.scope = scope;
    }
}
