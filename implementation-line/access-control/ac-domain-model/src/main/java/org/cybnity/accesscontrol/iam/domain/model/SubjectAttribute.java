package org.cybnity.accesscontrol.iam.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Attribute describing a subject who is demanding access (e.g role, group
 * membership, competency, user id, email address...).
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public abstract class SubjectAttribute {

    /**
     * Dedicated scope regarding this attribute.
     */
    private final IScopeAttribute scope;

    /**
     * Default constructor based on a scope.
     * 
     * @param scope Mandatory scope of this subject description criteria.
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     */
    public SubjectAttribute(IScopeAttribute scope) throws IllegalArgumentException {
	if (scope == null)
	    throw new IllegalArgumentException("Mandatory scope parameter shall be defined!");
	this.scope = scope;
    }
}
