package org.cybnity.accesscontrol.iam.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Represent a policy strategy type usable for a system control (e.g basic
 * network module). Is ideal for least privilege and need to know approach.
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_AC_3")
public class RoleBasedAccessControl extends AuthorizationPolicy {

}
