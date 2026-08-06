package org.cybnity.keycloak.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;
import org.keycloak.representations.idm.RoleRepresentation;

/**
 * A role represents organization-wide permissions or specific scope permissions.
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public class Role extends RoleRepresentation implements ExtendedResourcesDecorator {

    /**
     * Default constructor of Role controlled via Builder Pattern.
     * This extended class allow to host specific additional rules (e.g; limited values; security restriction on authorized values) applied to definition of a RoleRepresentation (controlled and maintained by Keycloak external project).
     *
     * @param builder Mandatory builder.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    Role(RoleBuilder builder) throws IllegalArgumentException {
        super();
        if (builder == null) throw new IllegalArgumentException("builder cannot be null");
        // Set the common values provided by the builder
        this.setName(builder.name);
        this.setDescription(builder.description);
    }

    /**
     * Apply rules of cleaning (also called Sanitization) on a label as required by Keycloak domain.
     * (e.g.; remove any space or special character to be usable as referenced by other object).
     *
     * @param label Mandatory label to eventually reformat.
     * @return The label value after cleaning.
     * @throws IllegalArgumentException When label parameter is null.
     */
    public static String applyNameSanitizationRequirements(String label) throws IllegalArgumentException {
        // Remove any potential special character (ensure all non-alphanumeric characters are removed)
        return Sanitizer.removeAllSpecialCharacters(label);
    }

    /**
     * Apply a decoration of this object with additional customization elements.
     */
    @Override
    public void decorate() {
    }
}
