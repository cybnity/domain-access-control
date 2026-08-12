package org.cybnity.keycloak.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;
import org.keycloak.representations.idm.RoleRepresentation;

/**
 * A realm role is global within a realm and applies to all clients in the realm. Realm role represents organization-wide permissions.
 * For example, type of role used for app user, offline access role, uma authorization, default realm admin roles.
 * <p>
 * Best usages:
 * For roles that are truly global
 * When multiple applications share the same role semantics
 * When permissions are organization-wide
 * <p>
 * Recommended: avoid using realm roles for app specific permissions.
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public class RealmRole extends RoleRepresentation implements ExtendedResourcesDecorator {

    /**
     * Default constructor of Role controlled via Builder Pattern.
     * This extended class allow to host specific additional rules (e.g; limited values; security restriction on authorized values) applied to definition of a RoleRepresentation (controlled and maintained by Keycloak external project).
     *
     * @param builder Mandatory builder.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    RealmRole(RealmRoleBuilder builder) throws IllegalArgumentException {
        super();
        if (builder == null) throw new IllegalArgumentException("builder cannot be null");
        // Set the common values provided by the builder
    }

    /**
     * Apply a decoration of this object with additional customization elements.
     */
    @Override
    public void decorate() {
    }
}
