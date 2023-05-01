package org.cybnity.accesscontrol.authorization.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Represent type of authorization usable by client-side (e.g web browser
 * javascript module). This type of authorization (e.g access token based) can
 * be intercepted if none security measure is implemented.
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_AC_4")
public interface Implicit extends AuthorizationType {

}
