package org.cybnity.keycloak.domain.model;

/**
 * Builder pattern implementation class allowing to respect the build rules of Keycloak regarding a RoleRepresentation object.
 * This class check the authorized values eligible for RealmRoleRepresentation build according to the format rules supported by Keycloak.
 * See specification of value supported at <a href="https://www.keycloak.org/docs-api/latest/rest-api/index.html#RoleRepresentation">RoleRepresentation object via JSON</a>.
 *
 * @author olivier
 */
public class RealmRoleBuilder extends RoleBuilder {

    // Extended specific Attributes, Users in role, Admin events can be added in this class

    /**
     * Default constructor.
     */
    public RealmRoleBuilder() {
        super();
    }

}
