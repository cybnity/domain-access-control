package org.cybnity.keycloak.domain.model;

import org.cybnity.framework.ConfigurationSource;
import org.cybnity.framework.UnoperationalStateException;
import org.keycloak.representations.idm.ClientRepresentation;

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
    public static String ATTR_X_FRAME_OPTIONS = "xFrameOptions";

    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String ATTR_FRONTEND_URL = "frontendUrl";

    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String ATTR_CONTENT_SECURITY_POLICY = "contentSecurityPolicy";

    String xFrameOptions;
    String frontEndURL;
    String contentSecurityPolicy;
    private final Map<String, String> defaultConf;

    /**
     * Default constructor.
     *
     * @param defaultConfigurationSource Mandatory provider of default configuration data.
     * @throws IllegalArgumentException    When mandatory parameter is missing.
     * @throws UnoperationalStateException When found configuration file does not include default properties (e.g; empty file).
     */
    public RealmWithDefaultExtendedResourcesBuilder(ConfigurationSource defaultConfigurationSource) throws IllegalArgumentException, UnoperationalStateException {
        super();
        if (defaultConfigurationSource == null)
            throw new IllegalArgumentException("defaultConfigurationSource is required!");
        defaultConf = defaultConfigurationSource.getProperties();
        if (defaultConf == null)
            throw new UnoperationalStateException("No default values found from file that are required for realm preparation!");
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
            attributes.put(RealmWithDefaultExtendedResourcesBuilder.ATTR_FRONTEND_URL, frontEndURL);

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
            attributes.put(RealmWithDefaultExtendedResourcesBuilder.ATTR_X_FRAME_OPTIONS, xFrameOptions);
        if (contentSecurityPolicy != null && !contentSecurityPolicy.isBlank())
            attributes.put(RealmWithDefaultExtendedResourcesBuilder.ATTR_CONTENT_SECURITY_POLICY, contentSecurityPolicy);

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

        // Add other default clients required by systems (e.g; reactive backend, AI endpoint) usable from UI layer or Application layer

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
        redirectUris.add(defaultConf.getOrDefault("REALM_DEFAULT_CLIENT_1_REDIRECT_URIS_1", null));
        redirectUris.add(defaultConf.getOrDefault("REALM_DEFAULT_CLIENT_1_REDIRECT_URIS_2", null));
        redirectUris.add(defaultConf.getOrDefault("REALM_DEFAULT_CLIENT_1_REDIRECT_URIS_3", null));

        List<String> postLogoutRedirectUris = new ArrayList<>();
        postLogoutRedirectUris.add(defaultConf.getOrDefault("REALM_DEFAULT_CLIENT_1_POSTLOGOUT_REDIRECT_URIS_1", null));
        postLogoutRedirectUris.add(defaultConf.getOrDefault("REALM_DEFAULT_CLIENT_1_POSTLOGOUT_REDIRECT_URIS_2", null));

        List<String> webOrigins = new ArrayList<>();
        webOrigins.add(defaultConf.getOrDefault("REALM_DEFAULT_CLIENT_1_WEBORIGINS_1", null));
        webOrigins.add(defaultConf.getOrDefault("REALM_DEFAULT_CLIENT_1_WEBORIGINS_2", null));

        webReactiveFrontEndSystemClient = new ClientBuilder()
                .clientId(defaultConf.getOrDefault("REALM_DEFAULT_CLIENT_1_CLIENTID", null))
                .name(defaultConf.getOrDefault("REALM_DEFAULT_CLIENT_1_NAME", null))
                .protocol(ClientBuilder.PROTOCOL_OPENID_CONNECT) /* Value under check rule */
                .description(defaultConf.getOrDefault("REALM_DEFAULT_CLIENT_1_DESCRIPTION", null))
                .alwaysDisplayInConsole(Boolean.parseBoolean(defaultConf.get("REALM_DEFAULT_CLIENT_1_IS_ALWAYS_DISPLAY_IN_CONSOLE")))
                .authorizationServicesEnabled(Boolean.parseBoolean(defaultConf.get("REALM_DEFAULT_CLIENT_1_IS_AUTHORIZATION_SERVICES_ENABLED")))
                .clientAuthenticationEnabled(Boolean.parseBoolean(defaultConf.get("REALM_DEFAULT_CLIENT_1_IS_CLIENT_AUTHENTICATION_ENABLED")))
                .isStandardAuthenticationFlow(Boolean.parseBoolean(defaultConf.get("REALM_DEFAULT_CLIENT_1_IS_STANDARD_AUTHENTICATION_FLOW")))
                .directAccessGrantsEnabled(Boolean.parseBoolean(defaultConf.get("REALM_DEFAULT_CLIENT_1_IS_DIRECT_ACCESS_GRANTS_ENABLED")))
                .rootUrl(defaultConf.getOrDefault("REALM_DEFAULT_CLIENT_1_ROOT_URL", null))
                .baseUrl(defaultConf.getOrDefault("REALM_DEFAULT_CLIENT_1_BASE_URL", null))
                .adminUrl(defaultConf.getOrDefault("REALM_DEFAULT_CLIENT_1_ADMIN_URL", null))
                .loginTheme(ClientBuilder.LOGIN_THEME_KEYCLOAKV2) /* Value under check rule */
                .frontChannelLogoutEnabled(Boolean.parseBoolean(defaultConf.get("REALM_DEFAULT_CLIENT_1_IS_FRONT_CHANNEL_LOGOUT_ENABLED")))
                .frontChannelLogoutSessionRequired(Boolean.parseBoolean(defaultConf.get("REALM_DEFAULT_CLIENT_1_IS_FRONT_CHANNEL_LOGOUT_SESSION_REQUIRED")))
                .redirectURIs(redirectUris)
                .postLogoutRedirectUris(postLogoutRedirectUris)
                .webOrigins(webOrigins)
                .build(); // return as default configuration
        return webReactiveFrontEndSystemClient;
    }

}
