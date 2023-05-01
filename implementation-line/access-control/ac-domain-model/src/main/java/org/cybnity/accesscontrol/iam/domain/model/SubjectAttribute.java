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
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_AC_3")
public interface SubjectAttribute {

}
