package org.cybnity.keycloak.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;
import org.keycloak.representations.idm.ClientRepresentation;

/**
 * Represents a client resource.
 * It's an extended ClientRepresentation that allow control of secured values to ensure CYBNITY / Keycloak compatibility.
 * The origin resource description (managed by Keycloak project managers) is available in API documentation provided by Keycloak at <a href="https://www.keycloak.org/docs-api/latest/rest-api/index.html#ClientRepresentation">ClientRepresentation</a>.
 *
 * @author olivier
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public class Client extends ClientRepresentation implements ExtendedResourcesDecorator {

    /**
     * Default constructor of Client controlled via Builder Pattern.
     * This extended class allow to host specific additional rules (e.g; limited values; security restriction on authorized values) applied to definition of a ClientRepresentation (controlled and maintained by Keycloak external project).
     *
     * @param builder Mandatory builder.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    Client(ClientBuilder builder) throws IllegalArgumentException {
        super();
        if (builder == null) throw new IllegalArgumentException("builder cannot be null");
        // Set the common values provided by the builder representing the default values for any client
        this.setClientId(builder.clientId);
        this.setName(builder.name);
        this.setProtocol(builder.protocol);
        this.setDescription(builder.description);
        this.setAlwaysDisplayInConsole(builder.alwaysDisplayInConsole);
        this.setAuthorizationServicesEnabled(builder.authorizationServicesEnabled);
        this.setPublicClient(builder.clientAuthenticationEnabled);
        this.setStandardFlowEnabled(builder.standardAuthenticationFlow);
        this.setDirectAccessGrantsEnabled(builder.directAccessGrantsEnabled);
        this.setRootUrl(builder.rootUrl);
        this.setBaseUrl(builder.baseUrl);
        this.setRedirectUris(builder.redirectURIs);
        this.setAttributes(builder.getAttributes());
        this.setWebOrigins(builder.webOrigins);
        this.setAdminUrl(builder.adminUrl);
        this.setFrontchannelLogout(builder.frontChannelLogoutEnabled);
    }

    /**
     * Apply rules of cleaning (also called Sanitization) on a label as required by Keycloak domain.
     *
     * @param label Mandatory label to reformat.
     * @return The label value after cleaning.
     * @throws IllegalArgumentException When label parameter is null.
     */
    public static String applyTextSanitizationRequirements(String label) throws IllegalArgumentException {
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
