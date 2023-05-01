package org.cybnity.accesscontrol.iam.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Atribute that represent a definition or criteria about an identity (e.g
 * company, person as subject of authorization).
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_AC_3")
public interface IdentityAttribute {

}
