package org.cybnity.accesscontrol.iam.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Represent a policy strategy type defined according to a regex pattern
 * respected by attribute(s) defined a subject.
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_AC_3")
public class RegexBasedAccessControl extends AuthorizationPolicy {

    private IdentityAttribute[] identityPattern;
}
