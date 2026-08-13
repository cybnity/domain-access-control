package org.cybnity.keycloak.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;
import org.keycloak.representations.idm.ClientScopeRepresentation;

/**
 * Client scopes is a common set of protocol mappers and roles that are shared between multiple clients.
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public class ClientScope extends ClientScopeRepresentation implements ExtendedResourcesDecorator {

    private final ClientScopeBuilder builder;

    /**
     * Default constructor of ClientScope controlled via Builder Pattern.
     * This extended class allow to host specific additional rules applied to definition of a ClientScopeRepresentation (controlled and maintained by Keycloak external project).
     *
     * @param builder Mandatory builder.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    ClientScope(ClientScopeBuilder builder) throws IllegalArgumentException {
        super();
        if (builder == null) throw new IllegalArgumentException("builder cannot be null");
        this.builder = builder;
        // Set the common values provided by the builder
        this.setName(builder.name);
        this.setDescription(builder.description);
        this.setProtocol(builder.protocol);

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
        this.setAttributes(builder.complementaryAttributes()); // all contents represented as attributes
        this.setProtocolMappers(builder.protocolMappers); // defined protocol mappers
    }

}
