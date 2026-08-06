package org.cybnity.keycloak.domain.model;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder pattern implementation class allowing to respect the build rules of Keycloak regarding a RealmRepresentation object, with decorator pattern usage added to the preparation phase of a Realm.
 * See keycloak-readmde.md documentation (from ac-adapter-keycloak-impl project) for help about basics required for Realm registration.
 *
 * @author olivier
 */
public class RealmWithDefaultExtendedResourcesBuilder extends RealmBuilder {

    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String X_FRAME_OPTIONS = "xFrameOptions";

    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String FRONTEND_URL = "frontendUrl";

    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String CONTENT_SECURITY_POLICY = "contentSecurityPolicy";

    String xFrameOptions;
    String frontEndURL;
    String contentSecurityPolicy;

    /**
     * Default constructor
     */
    public RealmWithDefaultExtendedResourcesBuilder() {
        super();
    }

    /**
     * Build and return prepared Realm configuration, including customization additional elements defining a default set of extended resources relative to the Realm.
     *
     * @return A RealmWithDefaultExtendedResources instance including customization elements.
     */
    public Realm build() {
        return new RealmWithDefaultExtendedResources(this);
    }

    /**
     * Get a container of attributes representing extended configuration of realm resource regarding General Settings.
     *
     * @return A set of attributes or null when none are defined.
     */
    public Map<String, String> generalSettings() {
        // Build instance of current values expected as realm resource attributes
        Map<String, String> attributes = new HashMap<>();

        /**
         *   "attributes": {
         *     "cibaBackchannelTokenDeliveryMode": "poll",
         *     "cibaAuthRequestedUserHint": "login_hint",
         *     "oauth2DevicePollingInterval": "5",
         *     "clientOfflineSessionMaxLifespan": "0",
         *     "clientSessionIdleTimeout": "0",
         *     "clientOfflineSessionIdleTimeout": "0",
         *     "cibaInterval": "5",
         *     "realmReusableOtpCode": "false",
         *     "cibaExpiresIn": "120",
         *     "oauth2DeviceCodeLifespan": "600",
         *     "saml.signature.algorithm": "",
         *     "parRequestUriLifespan": "60",
         *     "clientSessionMaxLifespan": "0",
         *     "frontendUrl": "http://dev.cybnity.tech/auth/",
         *     "acr.loa.map": "{}"
         *   }
         */

        // Add general settings into realm container
        if (frontEndURL != null && !frontEndURL.isBlank())
            attributes.put(RealmWithDefaultExtendedResourcesBuilder.FRONTEND_URL, frontEndURL);

        if (!attributes.isEmpty()) return attributes;
        return null; // default null attributes
    }

    /**
     * Set the frontend url for the realm that define the external (e.g; url and port exposed outside the K8s cluster) of Keycloak realm.
     * Realm settings element (e.g; http://10.101.238.65/auth/ regarding a host based on IP address).
     *
     * @param url url to frontend page url for a realm (e.g; http://dev.cybnity.tech/auth/ accessible from external network)
     * @return This builder.
     */
    public RealmWithDefaultExtendedResourcesBuilder frontEndUrl(String url) {
        if (url != null && !url.isBlank()) {
            this.frontEndURL = url;
        }
        return this;
    }

    /**
     * Get a container of attributes representing extended configuration of realm resource regarding Security Defense.
     *
     * @return A set of attributes or null when none are defined.
     */
    public Map<String, String> browserSecurityHeaders() {
        // Build instance of current values expected as realm resource attributes
        Map<String, String> attributes = new HashMap<>();

        /**
         * "browserSecurityHeaders": {
         *     "contentSecurityPolicyReportOnly": "",
         *     "xContentTypeOptions": "nosniff",
         *     "referrerPolicy": "no-referrer",
         *     "xRobotsTag": "none",
         *     "xFrameOptions": "SAMEORIGIN",
         *     "contentSecurityPolicy": "frame-src 'self'; frame-ancestors 'self'; object-src 'none';",
         *     "strictTransportSecurity": "max-age=31536000; includeSubDomains"
         *   }
         *
         */

        // Add security defense settings into realm container
        if (xFrameOptions != null && !xFrameOptions.isBlank())
            attributes.put(RealmWithDefaultExtendedResourcesBuilder.X_FRAME_OPTIONS, xFrameOptions);
        if (contentSecurityPolicy != null && !contentSecurityPolicy.isBlank())
            attributes.put(RealmWithDefaultExtendedResourcesBuilder.CONTENT_SECURITY_POLICY, contentSecurityPolicy);

        if (!attributes.isEmpty()) return attributes;
        return null; // default null attributes
    }

    /**
     * Set the xFrameOptions for the realm regarding browser security defense headers.
     * Realm security defense element.
     *
     * @param xFrameOptions Options to add in headers.
     * @return This builder.
     */
    public RealmWithDefaultExtendedResourcesBuilder xFrameOptions(String xFrameOptions) {
        if (xFrameOptions != null && !xFrameOptions.isBlank()) {
            this.xFrameOptions = xFrameOptions;
        }
        return this;
    }

    /**
     * Set the contentSecurityPolicy for the realm regarding content security defense headers.
     * Realm security defense element.
     *
     * @param contentSecurityPolicy Options to add in headers.
     * @return This builder.
     */
    public RealmWithDefaultExtendedResourcesBuilder contentSecurityPolicy(String contentSecurityPolicy) {
        if (contentSecurityPolicy != null && !contentSecurityPolicy.isBlank()) {
            this.contentSecurityPolicy = contentSecurityPolicy;
        }
        return this;
    }

    /**
     * Get connection clients dedicated to other CYBNITY systems, allowing communication from them to Keycloak server (e.g; from UI or domain layers).
     *
     * @return A list of client configurations.
     */
    public List<ClientRepresentation> systemsClientConfigurations() {
        List<ClientRepresentation> clients = new ArrayList<>();

        // Clients usable by UI layer components
        clients.add(webReactiveFrontEndSystemClient());

        // TODO Clients usable by application layer components

        return clients;
    }

    /**
     * Default prepared front end client (singleton instance)
     */
    Client webReactiveFrontEndSystemClient;

    /**
     * Prepare a client dedicated to CYBNITY web reactive front end system allowing end-users SSO tokens control requests to Keycloak.
     *
     * @return A client default configuration as singleton.
     */
    private ClientRepresentation webReactiveFrontEndSystemClient() {
        if (webReactiveFrontEndSystemClient != null) return webReactiveFrontEndSystemClient;

        // Prepare singleton instance
        List<String> redirectUris = new ArrayList<>();
        // TODO Change static value by read of envt variables
        redirectUris.add("http://dev-deploy.cybnity.tech/*");
        redirectUris.add("/*");
        redirectUris.add("http://dev-deploy.cybnity.tech:3000/*");

        List<String> postLogoutRedirectUris = new ArrayList<>();
        postLogoutRedirectUris.add("+");
        postLogoutRedirectUris.add("http://dev-deploy.cybnity.tech:3000/*");

        List<String> webOrigins = new ArrayList<>();
        webOrigins.add("+");
        webOrigins.add("http://dev-deploy.cybnity.tech:3000/*");

        webReactiveFrontEndSystemClient = new ClientBuilder()
                .clientId("web-reactive-frontend-system")
                .name("Web Reactive Frontend Client")
                .protocol(ClientBuilder.PROTOCOL_OPENID_CONNECT)
                .description("OpenID client supporting the user interface frontend systems")
                .alwaysDisplayInConsole(true)
                .authorizationServicesEnabled(false)
                .clientAuthenticationEnabled(false)
                .isStandardAuthenticationFlow(true)
                .directAccessGrantsEnabled(true)
                .rootUrl("${authBaseUrl}")
                .baseUrl("http://dev-deploy.cybnity.tech:3000/")
                .adminUrl("${authBaseUrl}")
                .loginTheme(ClientBuilder.LOGIN_THEME_KEYCLOAKV2)
                .frontChannelLogoutEnabled(true)
                .frontChannelLogoutSessionRequired(true)
                .redirectURIs(redirectUris)
                .postLogoutRedirectUris(postLogoutRedirectUris)
                .webOrigins(webOrigins)
                .build(); // return as default configuration
        return webReactiveFrontEndSystemClient;
    }

    /**
     * Prepare a list of default roles that are required for a realm usage.
     * For example, the roles assigned by default to any type of user and-or system roles (e.g; dedicated to environment or system types) required by CYBNITY application modules to use Keycloak authorization for access to specific resources.
     *
     * @return A list of transversal and default roles (e.g; "tenant-user" role) assignable to a realm.
     */
    public List<RoleRepresentation> tenantDefaultRealmRoles() {
        List<RoleRepresentation> roles = new ArrayList<>();
        // TODO Chante static roles definitions required by CYBNITY application and UI layers, for read from envt variables
        // doc: https://github.com/cybnity/domain-access-control/blob/feature-237/implementation-line/access-control/ac-domain-model/domain-model-components.md

        // Define basic role regarding any type of user authorized to use a tenant perimeter (equals to a realm scope)
        roles.add(new RealmRoleBuilder()
                .name(Sanitizer.removeAllBlankCharacters("tenant-user"))
                .description("Standard role of any type of user authorized to use a realm's contents perimeter")
                .build());
        return roles;
    }

}
