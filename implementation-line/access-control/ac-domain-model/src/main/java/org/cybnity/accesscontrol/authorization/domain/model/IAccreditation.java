package org.cybnity.accesscontrol.authorization.domain.model;

import org.cybnity.accesscontrol.iam.domain.model.SubjectAttribute;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

import java.util.Collection;

/**
 * Ability and role allowed to a JWT token owner.
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public interface IAccreditation {

    /**
     * Get description of the owner of this accreditation.
     * 
     * @return An owner identity.
     */
    Collection<SubjectAttribute> userIdentity();
}
