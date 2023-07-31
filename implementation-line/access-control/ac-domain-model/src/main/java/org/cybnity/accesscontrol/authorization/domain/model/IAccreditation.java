package org.cybnity.accesscontrol.authorization.domain.model;

import java.util.Collection;

import org.cybnity.accesscontrol.iam.domain.model.SubjectAttribute;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Habilitation and role allowed to a JWT token owner.
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
    public Collection<SubjectAttribute> userIdentity();
}