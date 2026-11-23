package org.cybnity.accesscontrol.authorization.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Represent model of authorization (e.g; requested, verified, accepted, rejected) followed by a stakeholder.
 * It's a type of authorized action (e.g; read, update, delete) also named privilege defined by a pair of action and resource specification (e.g; read:patients, or update:configuration, or delete:posts).
 *
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_AC_4")
public interface IAuthorizationType {

    /**
     * Description of this authorization type.
     *
     * @return A description or null.
     */
    public String description();

    /**
     * Name of this authorization type.
     * Generally respecting a naming convention (e.g; as role naming convention).
     * The definition of a consistent naming scheme for generic support of roles is based by type, by function, by environment or by combination.
     *
     * @return A logical name of this authorization type.
     */
    public String label();
}
