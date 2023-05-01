package org.cybnity.accesscontrol.authorization.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Represent an OIDC algorithm type for mobile application, web app, backend
 * token API used, and including 2 steps of realization.
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_AC_4")
public interface IAuthorizationCodeFlow extends IAuthorizationFlow {

}
