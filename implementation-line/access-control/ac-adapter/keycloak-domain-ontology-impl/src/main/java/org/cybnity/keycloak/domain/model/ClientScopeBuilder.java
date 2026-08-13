package org.cybnity.keycloak.domain.model;

import org.keycloak.representations.idm.ProtocolMapperRepresentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * +--------------------------------------------------------------+
 * |                      Token Issuance Pipeline                  |
 * +--------------------------------------------------------------+
 *    Authorization Request (scope=openid profile email teams)
 *         |
 *         v
 *  +--------------+     +---------------------------+
 *  | Default      |     | Optional Client Scopes    |
 *  | Client Scopes| <-- | (only when requested via  |
 *  +--------------+     |  the scope parameter)     |
 *         |             +---------------------------+
 *         +----------+----------+
 *                    v
 *         +---------------------+
 *         | Effective Scope Set |
 *         +---------------------+
 *                    |
 *                    v
 *         +---------------------+      +--------------------+
 *         | Protocol Mappers    | <--- | Dedicated Mappers  |
 *         | (linked to scopes)  |      | (client-specific)  |
 *         +---------------------+      +--------------------+
 *                    |
 *                    v
 *    +-----------------------------------------+
 *    | ID Token / Access Token / UserInfo      |
 *    +-----------------------------------------+
 */
public class ClientScopeBuilder {

    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String ATTR_INCLUDE_IN_OPENID_PROVIDER_METADATA = "include.in.openid.provider.metadata";
    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String ATTR_DISPLAY_ORDER = "gui.order";
    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String ATTR_DISPLAY_ON_CONSENT_SCREEN = "display.on.consent.screen";
    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String ATTR_INCLUDE_IN_TOKEN_SCOPE = "include.in.token.scope";
    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String ATTR_CONSENT_SCREEN_TEXT = "consent.screen.text";
    /**
     * SSO protocol configuration supplied by this client scope.
     */
    public static String PROTOCOL_OPENIDCONNECT = "openid-connect";
    /**
     * SSO protocol configuration supplied by this client scope.
     */
    public static String PROTOCOL_SAML = "saml";
    /**
     * Client scopes, which will be added as default scopes to each created client.
     */
    public static String TYPE_DEFAULT = "default";
    /**
     * Client scopes, which will be added sometime to created client.
     */
    public static String TYPE_OPTIONAL = "optional";
    /**
     * Client scopes, which will not be added as default scopes to each created client.
     */
    public static String TYPE_NONE = "none";
    String name;
    String description;
    String protocol;
    String type;
    String consentScreenText;
    Boolean isIncludedInTokenScope = false;
    Boolean isDisplayOnConsentScreen = false;
    Boolean isIncludedInOpenIDProviderMetadata = true;
    Integer displayOrder;
    List<ProtocolMapperRepresentation> protocolMappers;

    /**
     * Default constructor.
     */
    public ClientScopeBuilder() {
    }

    /**
     * Prepare and return an instance of ClientScope.
     *
     * @return A realm object including default configuration settings defined.
     */
    public ClientScope build() {
        return new ClientScope(this);
    }

    /**
     * The name of the ClientScope.
     * This method only apply basic Keycloak minimum sanitization rule that check is real name is not empty and does not contain space character.
     * To ensure better sanitization of scope name, use {@link Sanitizer#removeAllBlankCharacters(String)} method before to clean the name about multiple special characters generating potential problem during its usage.
     * <p>
     * Must be unique in the realm. Name should not contain space characters as it is used as value of scope parameter.
     *
     * @param clientScopeName A mandatory defined logical name.
     * @return This builder instance.
     * @throws IllegalArgumentException When clientScopeName parameter value is empty or does not respect format rule (e.g; not empty, not blank character).
     */
    public ClientScopeBuilder name(String clientScopeName) throws IllegalArgumentException {
        if (clientScopeName == null)
            throw new IllegalArgumentException("The name parameter is required!");

        // Check supported value and authorized formatting rules
        if (clientScopeName.isEmpty()) // Not empty
            throw new IllegalArgumentException("The name value shall not be empty!");

        if (clientScopeName.contains(" ")) // Not blank character
            throw new IllegalArgumentException("The name value shall not contain spaces!");

        this.name = clientScopeName;
        return this;
    }

    /**
     * The description of the client scope.
     *
     * @param description A textual description helping to understand what goal, and usage are concerned by the client scope.
     * @return This builder instance.
     */
    public ClientScopeBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * The protocol of the client scope.
     * SSO protocol configuration is being supplied by this client scope.
     *
     * @param protocol Value that is checked about authorized value. Ignored if invalid value.
     * @return This builder instance.
     */
    public ClientScopeBuilder protocol(String protocol) {
        if (protocol != null) {
            if (PROTOCOL_OPENIDCONNECT.equalsIgnoreCase(protocol) || PROTOCOL_SAML.equalsIgnoreCase(protocol)) {
                this.protocol = protocol;
            }
        }
        return this;
    }

    /**
     * Client scopes, which will be added as default, optional or scopes to each created client.
     *
     * @param clientType Value that is checked about authorized value. Ignored if invalid value.
     * @return This builder instance.
     */
    public ClientScopeBuilder type(String clientType) {
        if (clientType != null) {
            if (TYPE_DEFAULT.equals(clientType) || TYPE_OPTIONAL.equals(clientType) || TYPE_NONE.equals(clientType)) {
                this.type = clientType;
            }
        }
        return this;
    }

    /**
     * Text that will be shown on the consent screen when this client scope is added to some client with consent required. Defaults to name of client scope if it is not filled.
     *
     * @param label A text.
     * @return This builder instance.
     */
    public ClientScopeBuilder consentScreenText(String label) {
        this.consentScreenText = label;
        return this;
    }

    /**
     * Protocol mappers to associate with client scope.
     *
     * @param protocolMappers List fo mappers.
     * @return This builder instance.
     */
    public ClientScopeBuilder protocolMappers(List<ProtocolMapperRepresentation> protocolMappers) {
        this.protocolMappers = protocolMappers;
        return this;
    }

    /**
     * If on, the name of this client scope will be added to the access token property 'scope' as well as to the Token Introspection Endpoint response.
     * Not included by default.
     *
     * @param isIncluded If false, this client scope will be omitted from the token and from the Token Introspection Endpoint response.
     * @return This builder instance.
     */
    public ClientScopeBuilder includedInTokenScope(boolean isIncluded) {
        this.isIncludedInTokenScope = isIncluded;
        return this;
    }

    /**
     * Define if consent required and need to be shown on consent screen.
     * Not displayed by default.
     *
     * @param isDisplayOnConsentScreen If true, and this client scope is added to some client with consent required, the text specified by 'Consent Screen Text' will be displayed on consent screen. If off, this client scope will not be displayed on the consent screen.
     * @return This builder instance.
     */
    public ClientScopeBuilder displayOnConsentScreen(boolean isDisplayOnConsentScreen) {
        this.isDisplayOnConsentScreen = isDisplayOnConsentScreen;
        return this;
    }

    /**
     * Specify order of the provider in GUI (such as in Consent page) as integer.
     * None defined by default.
     *
     * @param position A position.
     * @return This builder instance.
     */
    public ClientScopeBuilder displayOrder(Integer position) {
        this.displayOrder = position;
        return this;
    }

    /**
     * Define inclusion of client scope into provider metadata.
     * Included by default.
     *
     * @param isIncludedInOpenIDProviderMetadata If true, this client scope will be included in OpenID Provider Metadata.
     * @return This builder instance.
     */
    public ClientScopeBuilder isIncludedInOpenIDProviderMetadata(boolean isIncludedInOpenIDProviderMetadata) {
        this.isIncludedInOpenIDProviderMetadata = isIncludedInOpenIDProviderMetadata;
        return this;
    }

    /**
     * Get a container of attributes representing extended configuration.
     *
     * @return A set of attributes or null when none are defined.
     */
    public Map<String, String> complementaryAttributes() {
        // Build instance of current values expected as client scope resource attributes
        Map<String, String> attributes = new HashMap<>();

        /* {
         "id": "327ddf80-5c60-4394-ac3d-9ae2df797b8e",
         "name": "ui-layer-system-roles",
         "description": "OpenID Connect build-in scope about the common roles of clients used by systems of UI layer",
         "protocol": "openid-connect",
         "attributes": {
         "include.in.token.scope": "true",
         "display.on.consent.screen": "true",
         "gui.order": "",
         "consent.screen.text": "a consent description",
         "include.in.openid.provider.metadata": "true"
         } */

        attributes.put(ATTR_INCLUDE_IN_TOKEN_SCOPE, isIncludedInTokenScope.toString());
        attributes.put(ATTR_DISPLAY_ON_CONSENT_SCREEN, isDisplayOnConsentScreen.toString());
        attributes.put(ATTR_INCLUDE_IN_OPENID_PROVIDER_METADATA, isIncludedInOpenIDProviderMetadata.toString());

        if (consentScreenText != null && !consentScreenText.isBlank())
            attributes.put(ATTR_CONSENT_SCREEN_TEXT, consentScreenText);
        if (displayOrder != null)
            attributes.put(ATTR_DISPLAY_ORDER, displayOrder.toString());

        return attributes;
    }

    /**
     * Add a mapper to the client scope prepared.
     *
     * @param protocolMapper Mandatory mapper.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    private void addProtocolMapper(ProtocolMapperRepresentation protocolMapper) throws IllegalArgumentException {
        if (protocolMapper == null) {
            throw new IllegalArgumentException("protocolMapper parameter cannot be null!");
        }
        if (this.protocolMappers == null) {
            this.protocolMappers = new ArrayList<>();
        }
        this.protocolMappers.add(protocolMapper);
    }

    /**
     * Execute the build of a mapper and assign it to the prepared client scope.
     *
     * @param builder Mandatory builder to execute.
     * @return This builder instance.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public ClientScopeBuilder protocolMapper(ProtocolMapperBuilder builder) throws IllegalArgumentException {
        if (builder == null) {
            throw new IllegalArgumentException("builder parameter cannot be null!");
        }
        // Execute the build of mapper, and add into the prepared mappers
        this.addProtocolMapper(builder.build());
        return this;
    }
}
