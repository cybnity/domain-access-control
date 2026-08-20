package org.cybnity.keycloak.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;
import org.keycloak.representations.idm.RoleRepresentation;

/**
 * A role represents organization-wide permissions or specific scope permissions.
 * For example, can represent a common IAuthorizationType of Access Control domain, based on a PermissionCode (e.g; PermissionCode.USE_TENANT).
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
        if (builder.isClientRole != null)
            this.setClientRole(builder.isClientRole);
        if (builder.isComposite != null)
            this.setComposite(builder.isComposite);
        // Apply complementary customization of default value not already defined dynamically by the builder
        decorate();
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
     * This method is responsible to create the customization rules (e.g; instantiation of additional customization elements to this object; or to change some current object's attributes) and shall be implemented by any subclass as a concrete decorator pattern implementation.
     */
    @Override
    public void decorate() {
        // Dynamic defined attributes as extended customization elements

    }

}
