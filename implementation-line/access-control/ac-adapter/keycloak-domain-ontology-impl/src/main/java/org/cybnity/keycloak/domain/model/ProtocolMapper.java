package org.cybnity.keycloak.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;

/**
 * Protocol mapper reference to an existing protocol mapper type.
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public class ProtocolMapper extends ProtocolMapperRepresentation implements ExtendedResourcesDecorator {

    private final ProtocolMapperBuilder builder;

    /**
     * Default constructor via Builder Pattern.
     * This extended class allow to host specific additional rules applied to definition of a ProtocolMapperRepresentation (controlled and maintained by Keycloak external project).
     *
     * @param builder Mandatory builder.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    ProtocolMapper(ProtocolMapperBuilder builder) throws IllegalArgumentException {
        super();
        if (builder == null) throw new IllegalArgumentException("builder cannot be null");
        this.builder = builder;
        // Set the common values provided by the builder
        this.setProtocolMapper(builder.protocolMapperTypeReferenceName);
        this.setName(builder.mapperName);
        this.setProtocol(builder.protocol);

        // Apply complementary customization of default value not already defined dynamically by the builder
        decorate();
    }

    /**
     * Apply a decoration of this object with additional customization elements.
     * This method is responsible to create the customization rules (e.g; instantiation of additional customization elements to this object; or to change some current object's attributes) and shall be implemented by any subclass as a concrete decorator pattern implementation.
     */
    @Override
    public void decorate() {
        // Dynamic defined attributes as extended customization elements
        this.setConfig(builder.config());
    }

}
