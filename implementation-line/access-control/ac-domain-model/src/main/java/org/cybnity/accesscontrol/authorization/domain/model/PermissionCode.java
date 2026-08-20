package org.cybnity.accesscontrol.authorization.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Common authorization (also named permission), based on a naming convention for naming, and reusable by applications and-or automated flow for default assigning to actors (e.g; security accessories, basic users).
 * For example, usable by server-side client application (e.g; front UI or backend server module) as common assignable transversal role.
 *
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_AC_4")
public enum PermissionCode implements IAuthorizationType {

    /**
     * Tenant authorized usage.
     */
    USE_TENANT("use-tenant", null),

    /**
     * Authorized access to applications.
     */
    ACCESS_APPLICATIONS("access-applications", null);

    /**
     * Authorization logical name.
     */
    final String label;

    /**
     * Authorization description.
     */
    final String description;

    /**
     * Default construction of common codified authorization type.
     *
     * @param label       Mandatory name.
     * @param description Optional description.
     */
    PermissionCode(String label, String description) {
        this.label = label;
        this.description = description;
    }

    @Override
    public String description() {
        return this.description;
    }

    @Override
    public String label() {
        return this.label;
    }
}
