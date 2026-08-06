package org.cybnity.keycloak.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;
import org.keycloak.representations.idm.RoleRepresentation;

/**
 * A client role is specific to one application (client). This role is scoped to a single client. Same user can have different roles in different apps, ideal for microservice and modern architectures.
 * For example, type of role used for application component custom role.
 * Or for example about sub-roles supported/used by a client scope dedicated for specific access to set of a component's or domain's features (e.g; manage-account, manage-account-links, view-profile feature roles).
 * <p>
 * Best usages:
 * For authorization that is application-specific
 * For microservices or multiple apps
 * For designing a production system
 * When role isolation matters
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public class ClientRole extends RoleRepresentation implements ExtendedResourcesDecorator {
    /**
     * Apply a decoration of this object with additional customization elements.
     */
    @Override
    public void decorate() {

    }
}
