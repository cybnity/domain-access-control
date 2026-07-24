package org.cybnity.keycloak.domain.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder pattern implementation class allowing to respect the build rules of Keycloak regarding a ClientRepresentation object.
 * See example of value supported at <a href="https://www.keycloak.org/docs-api/latest/rest-api/index.html#ClientRepresentation">ClientRepresentation object via JSON</a>.
 *
 * @author olivier
 */
public class ClientBuilder {

    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute value.
     * Protocol that allow Clients to verify the identity of the End-User based on the authentication performed by an Authorization Server.
     */
    public static String PROTOCOL_OPENID_CONNECT = "openid-connect";

    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String POST_LOGOUT_REDIRECT_URIS = "post.logout.redirect.uris";

    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String LOGIN_THEME = "login_theme";

    /**
     * Supported theme for login, OTP, grant, registration and forgot password pages.
     */
    public static String LOGIN_THEME_KEYCLOAK = "keycloak", LOGIN_THEME_KEYCLOAKV2 = "keycloak.v2", LOGIN_THEME_BASE = "base";

    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String FRONT_CHANNEL_LOGIN_SESSION_REQUIRED = "frontchannel.logout.session.required";

    String clientId;
    String name;
    String protocol;
    String description;
    boolean alwaysDisplayInConsole = false;
    boolean authorizationServicesEnabled = false;
    boolean clientAuthenticationEnabled = false;
    boolean standardAuthenticationFlow = false;
    boolean directAccessGrantsEnabled = false;
    boolean frontChannelLogoutEnabled = false;
    Boolean frontChannelLogoutSessionRequired;

    String rootUrl;
    String baseUrl;
    String adminUrl;
    List<String> redirectURIs;
    List<String> postLogoutRedirectUris;
    List<String> webOrigins;
    String loginTheme;

    /**
     * Default constructor.
     */
    public ClientBuilder() {
    }

    /*** Prepare and return an instance of Client.
     *
     * @return A client object including default configuration settings defined.
     */
    public Client build() {
        return new Client(this);
    }

    /**
     * The identifier of the Client that specify ID referenced in URI and tokens.
     * This method only apply basic Keycloak minimum sanitization rule that check is client identified is not empty and does not contain space character.
     * To ensure better sanitization of client id, use {@link Client#applyTextSanitizationRequirements(String)} method before to clean the id label about multiple special characters.
     *
     * @param id A mandatory defined logical and unique identifier.
     * @return This builder instance.
     * @throws IllegalArgumentException When id parameter value is empty or does not respect format rule (e.g; not empty, not blank character).
     */
    public ClientBuilder clientId(String id) throws IllegalArgumentException {
        if (id == null) throw new IllegalArgumentException("id parameter is required!");
        // Check supported value and authorized formatting rules
        if (id.isEmpty()) // Not empty
            throw new IllegalArgumentException("The id value shall not be empty!");
        if (id.contains(" ")) // Not blank character
            throw new IllegalArgumentException("The id value shall not contain spaces!");
        this.clientId = id;
        return this;
    }

    /**
     * The logical name of the Client that specify display name of the client.
     *
     * @param clientName A mandatory defined logical name.
     * @return This builder instance.
     *
     */
    public ClientBuilder name(String clientName) {
        this.name = clientName;
        return this;
    }

    /**
     * Optional type of protocol.
     * Each client has a protocol associated defining its client type.
     *
     * @param protocol Protocol type definition (e.g; CientBuilder.PROTOCOL_OPENID_CONNECT as "openid-connect" protocol).
     * @return This builder instance.
     * @throws IllegalArgumentException When protocol value is not supported value (PROTOCOL_OPENID_CONNECT).
     */
    public ClientBuilder protocol(String protocol) throws IllegalArgumentException {
        if (protocol != null) {
            // Check value conformity
            if (!PROTOCOL_OPENID_CONNECT.equals(protocol))
                throw new IllegalArgumentException("The protocol type parameter is invalid!");
        }
        this.protocol = protocol;
        return this;
    }

    /**
     * Optional description of the client.
     *
     * @param description A description. Ignored when null.
     * @return This builder instance.
     */
    public ClientBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Define theme for login, OTP, grant, registration and forgot password pages.
     *
     * @param loginTheme A theme name supported by Keycloak
     * @return A theme.
     * @throws IllegalArgumentException When loginTheme parameter value is not supported (see LOGIN_THEME_BASE, LOGIN_THEME_KEYCLOAK, LOGIN_THEME_KEYCLOAKV2 authorized value).
     */
    public ClientBuilder loginTheme(String loginTheme) throws IllegalArgumentException {
        if (loginTheme != null) {
            // Check value conformity
            if (!LOGIN_THEME_KEYCLOAKV2.equals(loginTheme) && !LOGIN_THEME_BASE.equals(loginTheme) && !LOGIN_THEME_KEYCLOAK.equals(loginTheme)) {
                throw new IllegalArgumentException("The loginTheme parameter is invalid!");
            }
        }
        this.loginTheme = loginTheme;
        return this;

    }

    /**
     * Define that client always listed in the Account Console, even if the user does not have an active session.
     *
     * @param alwaysDisplayInConsole True when shall be displayed in account console. False by default.
     * @return This builder instance.
     */
    public ClientBuilder alwaysDisplayInConsole(boolean alwaysDisplayInConsole) {
        this.alwaysDisplayInConsole = alwaysDisplayInConsole;
        return this;
    }

    /**
     * Enable/Disable fine-grained authorization support for a client.
     *
     * @param authorizationServicesEnabled True when authorization support is required.
     * @return This builder instance.
     */
    public ClientBuilder authorizationServicesEnabled(boolean authorizationServicesEnabled) {
        this.authorizationServicesEnabled = authorizationServicesEnabled;
        return this;
    }

    /**
     * When true, logout requires a browser to send the request to the client to configured Front-channel logout URL as specified in the OIDC Front-channel logout specification.
     * When false, server can perform a background invocation for logout as long as either the Backchannel-logout URL is configured or Admin URL is configured.
     *
     * @param frontChannelLogoutEnabled Is enabled.
     * @return This builder instance.
     */
    public ClientBuilder frontChannelLogoutEnabled(boolean frontChannelLogoutEnabled) {
        this.frontChannelLogoutEnabled = frontChannelLogoutEnabled;
        return this;
    }

    /**
     * Specifying whether a sid (session ID) and iss (issuer) parameters are included in the Logout request when the Front-channel Logout URL is used.
     *
     * @param frontChannelLogoutSessionRequired Enabled or disabled.
     * @return This builder instance.
     */
    public ClientBuilder frontChannelLogoutSessionRequired(boolean frontChannelLogoutSessionRequired) {
        this.frontChannelLogoutSessionRequired = frontChannelLogoutSessionRequired;
        return this;
    }

    /**
     * Defines the type of OIDC client.
     *
     * @param clientAuthenticationEnabled When it is enabled, the type is confidential access. When it is disabled, the type is public access.
     * @return This builder instance.
     */
    public ClientBuilder clientAuthenticationEnabled(boolean clientAuthenticationEnabled) {
        this.clientAuthenticationEnabled = clientAuthenticationEnabled;
        return this;
    }

    /**
     * This enables standard OpenID Connect redirect based authentication with authorization code.
     * In terms of OpenID Connect or OAuth2 specifications, this enables support of 'Authorization Code Flow' for this client.
     *
     * @param enabled True when authentication standard flow is enabled.
     * @return This builder instance.
     */
    public ClientBuilder isStandardAuthenticationFlow(boolean enabled) {
        this.standardAuthenticationFlow = enabled;
        return this;
    }

    /**
     * This enables support for Direct Access Grants, which means that client has access to username/password of user and exchange it directly with Keycloak server for access token.
     * In terms of OAuth2 specification, this enables support of 'Resource Owner Password Credentials Grant' for this client.
     *
     * @param enabled True when enabled.
     * @return This builder instance.
     */
    public ClientBuilder directAccessGrantsEnabled(boolean enabled) {
        this.directAccessGrantsEnabled = enabled;
        return this;
    }

    /**
     * Define root URL appended to relative URLs.
     *
     * @param rootUrl An url. Defined according to the external port exposed by the web-reactive-frontend-system module executed into the K8s cluster.
     * @return This builder instance.
     */
    public ClientBuilder rootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
        return this;
    }

    /**
     * URL to the admin interface of the client.
     * Set this if the client supports the adapter REST API.
     * This REST API allows the auth server to push revocation policies and other administrative tasks.
     * Usually this is set to the base URL of the client.
     *
     * @param adminUrl An URL.
     * @return This builder instance.
     */
    public ClientBuilder adminUrl(String adminUrl) {
        this.adminUrl = adminUrl;
        return this;
    }

    /**
     * Define the home URL.
     *
     * @param baseUrl Default URL to use when the auth server needs to redirect or link back to the client.
     * @return This builder instance.
     */
    public ClientBuilder baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * Define list of URIs for redirections.
     * Valid URI pattern a browser can redirect to after a successful login.
     * Simple wildcards are allowed such as '<a href="http://example.com/">example</a>*'.
     * Relative path can be specified too such as /my/relative/path/*.
     * Relative paths are relative to the client root URL, or if none is specified the auth server root URL is used.
     * For SAML, you must set valid URI patterns if you are relying on the consumer service URL embedded with the login request.
     *
     * @param redirectURIs List of uri.
     * @return This builder instance.
     */
    public ClientBuilder redirectURIs(List<String> redirectURIs) {
        this.redirectURIs = redirectURIs;
        return this;
    }

    /**
     * Valid URI pattern a browser can redirect to after a successful logout.
     * A value of '+' or an empty field uses the list of valid redirect URIs.
     * A value of '-' does not allow any post logout redirect URIs. Simple wildcards are allowed such as '<a href="http://example.com/">example</a>*'.
     * A relative path can be specified too, such as /my/relative/path/*.
     * Relative paths are relative to the client root URL; if none is specified, the auth server root URL is used.
     *
     * @param uris Set of URIs and or patterns (e.g; "+" or "http://xxxxx").
     * @return This builder instance.
     */
    public ClientBuilder postLogoutRedirectUris(List<String> uris) {
        this.postLogoutRedirectUris = uris;
        return this;
    }

    /**
     * Allowed CORS origins.
     * To permit all origins of Valid Redirect URIs, add '+'.
     * This does not include the '*' wildcard though.
     * To permit all origins, explicitly add '*'.
     *
     * @param webOrigins A list of web origin URIs and-or patterns.
     * @return This builder instance.
     */
    public ClientBuilder webOrigins(List<String> webOrigins) {
        this.webOrigins = webOrigins;
        return this;
    }

    /**
     * Get URIs and pattern in a combined version supported by Keycloak Admin API as value for post logout redirect URIs.
     * This method apply the format pattern supported by Keycloak an insert special character type between each existing post logout redirect URI.
     *
     * @return A combined version in format supported by Keycloak API (e.g; each URI or pattern with separator character "##" applied). Return null when none post logout redirect URI is defined.
     */
    private String postLogoutRedirectUrisInCombinedVersion() {
        if (this.postLogoutRedirectUris != null && !this.postLogoutRedirectUris.isEmpty()) {
            StringBuffer combinedURIsPatterns = new StringBuffer();
            this.postLogoutRedirectUris.forEach(uriOrPattern -> {
                if (combinedURIsPatterns.length() > 0) {
                    // Add separator character
                    combinedURIsPatterns.append("##"); // Keycloak supported separator
                }
                combinedURIsPatterns.append(uriOrPattern);
            });
            return combinedURIsPatterns.toString(); // Return combined URIs and-or pattern value
        }
        return null; // None defined
    }

    /**
     * Get a container of attributes representing extended configuration of client resource.
     *
     * @return A set of attributes or null when none are defined.
     */
    public Map<String, String> getAttributes() {
        Map<String, String> attributes = new HashMap<>();

        /**
         * "attributes": {
         *     "logout.confirmation.enabled": "false",
         *     "client.secret.creation.time": "1784291204",
         *     "standard.token.exchange.enabled": false,
         *     "oauth2.jwt.authorization.grant.enabled": false,
         *     "login_theme": "keycloak.v2",
         *     "post.logout.redirect.uris": "+##http://dev-deploy.cybnity.tech/private/*##http://test",
         *     "frontchannel.logout.session.required": "true",
         *     "oauth2.device.authorization.grant.enabled": "false",
         *     "backchannel.logout.revoke.offline.tokens": "false",
         *     "realm_client": "false",
         *     "oidc.ciba.grant.enabled": false,
         *     "backchannel.logout.session.required": "true",
         *     "display.on.consent.screen": "false",
         *     "dpop.bound.access.tokens": "false",
         *     "pkce.code.challenge.method": "",
         *     "consent.screen.text": "",
         *     "frontchannel.logout.url": ""
         *   }
         */

        String postLogoutRedirectUrisString = postLogoutRedirectUrisInCombinedVersion();
        if (postLogoutRedirectUrisString != null) {
            attributes.put(POST_LOGOUT_REDIRECT_URIS, postLogoutRedirectUrisString);
        }
        if (this.loginTheme != null && !loginTheme.isEmpty()) {
            attributes.put(LOGIN_THEME, loginTheme);
        }
        if (this.frontChannelLogoutSessionRequired != null)
            attributes.put(FRONT_CHANNEL_LOGIN_SESSION_REQUIRED, this.frontChannelLogoutSessionRequired.toString());

        if (!attributes.isEmpty()) return attributes;
        return null;
    }

}

/**
 * EXAMPLE OF CLIENT JSON FOR HELP
 * {
 * "clientId": "web-reactive-frontend-system",
 * "name": "Web Reactive Frontend Client",
 * "description": "OpenID client supporting the user interface frontend systems",
 * "rootUrl": "${authBaseUrl}",
 * "adminUrl": "${authBaseUrl}",
 * "baseUrl": "http://dev-deploy.cybnity.tech:3000/",
 * "surrogateAuthRequired": false,
 * "enabled": true,
 * "alwaysDisplayInConsole": true,
 * "clientAuthenticatorType": "client-secret",
 * "redirectUris": [
 * "http://dev-deploy.cybnity.tech/*",
 * "/*",
 * "http://dev-deploy.cybnity.tech:3000/*"
 * ],
 * "webOrigins": [
 * "+"
 * ],
 * "notBefore": 0,
 * "bearerOnly": false,
 * "consentRequired": false,
 * "standardFlowEnabled": true,
 * "implicitFlowEnabled": false,
 * "directAccessGrantsEnabled": true,
 * "serviceAccountsEnabled": false,
 * "publicClient": true,
 * "frontchannelLogout": true,
 * "protocol": "openid-connect",
 * "attributes": {
 * "logout.confirmation.enabled": "false",
 * "client.secret.creation.time": "1784291204",
 * "standard.token.exchange.enabled": "false",
 * "oauth2.jwt.authorization.grant.enabled": "false",
 * "login_theme": "keycloak.v2",
 * "post.logout.redirect.uris": "+",
 * "frontchannel.logout.session.required": "true",
 * "oauth2.device.authorization.grant.enabled": "false",
 * "backchannel.logout.revoke.offline.tokens": "false",
 * "realm_client": "false",
 * "oidc.ciba.grant.enabled": "false",
 * "backchannel.logout.session.required": "true",
 * "display.on.consent.screen": "false",
 * "dpop.bound.access.tokens": "false"
 * },
 * "authenticationFlowBindingOverrides": {},
 * "fullScopeAllowed": true,
 * "nodeReRegistrationTimeout": -1,
 * "defaultClientScopes": [
 * "web-origins",
 * "acr",
 * "roles",
 * "profile",
 * "basic",
 * "email"
 * ],
 * "optionalClientScopes": [
 * "address",
 * "phone",
 * "organization",
 * "offline_access",
 * "microprofile-jwt"
 * ],
 * "access": {
 * "view": true,
 * "configure": true,
 * "manage": true
 * }
 * }
 */