package org.cybnity.accesscontrol.iam.domain.model;

import org.cybnity.framework.INaming;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Combination of attributes describing what actor want to perform (e.g; read, write, any action type regarding a resource).
 *
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_AC_3")
public interface IActionAttribute extends INaming {

    /**
     * Description of this action type.
     *
     * @return A description or null.
     */
    public String description();
}
