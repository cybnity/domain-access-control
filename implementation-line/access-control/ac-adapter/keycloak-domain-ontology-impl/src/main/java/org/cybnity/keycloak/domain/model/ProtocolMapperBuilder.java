package org.cybnity.keycloak.domain.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
public class ProtocolMapperBuilder {
    /**
     * SSO protocol configuration supplied by this mapper.
     */
    public static String PROTOCOL_OPENIDCONNECT = "openid-connect";
    /**
     * SSO protocol configuration supplied by this mapper.
     */
    public static String PROTOCOL_SAML = "saml";
    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String CONFIG_IS_ADDED_TO_INTROSPECTION = "introspection.token.claim";
    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String CONFIG_IS_MULTIVALUED = "multivalued";
    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String CONFIG_IS_ADDED_TO_USERINFO = "userinfo.token.claim";
    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String CONFIG_IS_CLAIM_ADDED_TO_TOKEN_ID = "id.token.claim";
    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String CONFIG_IS_ADDED_TO_LIGHTWEIGHT_ACCESS_TOKEN = "lightweight.claim";
    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String CONFIG_IS_ADDED_TO_ACCESS_TOKEN = "access.token.claim";
    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String CONFIG_TOKEN_CLAIM_NAME = "claim.name";
    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String CONFIG_CLAIM_JSON_TYPE = "jsonType.label";
    /**
     * Keycloak API JSON specification (Keycloak API project defined/controled) attribute name.
     */
    public static String CONFIG_CLIENT_ID = "usermodel.clientRoleMapping.clientId";

    /**
     * Unique name of existing mapper type taken as reference by the mapper to prepare.
     */
    String protocolMapperTypeReferenceName;
    String mapperName;
    private String clientId;
    private Boolean isMultivalued = Boolean.FALSE;
    private String tokenClaimName;
    private String claimJSONType = "String";
    private Boolean isAddedToIDToken = Boolean.FALSE;
    private Boolean isAddedToAccessToken = Boolean.FALSE;
    private Boolean isAddedToLightweightAccessToken = Boolean.FALSE;
    private Boolean isAddedToUserinfo = Boolean.FALSE;
    private Boolean isAddedToTokenIntrospection = Boolean.FALSE;
    String protocol;

    /**
     * Default constructor.
     */
    public ProtocolMapperBuilder() {
    }

    /**
     * Prepare and return an instance of ClientScope.
     *
     * @return A realm object including default configuration settings defined.
     */
    public ProtocolMapper build() {
        return new ProtocolMapper(this);
    }

    /**
     * Reference name of existing mapper type considered as protocol mapper to prepare.
     * See <a href="https://www.keycloak.org/admin-api/protocol-mappers">protocol mappers available by default into Keycloak</a>.
     * @param protocolMapperTypeReferenceName Mapper type name.
     * @return This builder instance.
     */
    public ProtocolMapperBuilder mapperTypeReferenceName(String protocolMapperTypeReferenceName) {
        this.protocolMapperTypeReferenceName = protocolMapperTypeReferenceName;
        return this;
    }

    /**
     * Unique name of the mapper.
     *
     * @param mapperName A label.
     * @return This builder instance.
     */
    public ProtocolMapperBuilder mapperName(String mapperName) {
        this.mapperName = mapperName;
        return this;
    }

    /**
     * Client ID for role mappings.
     * Just client roles of this client will be added to the token. If this is unset, client roles of all clients will be added to the token.
     *
     * @param clientId Existing client id (name).
     * @return This builder instance.
     */
    public ProtocolMapperBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Indicates if attribute supports multiple values.
     * False by default.
     *
     * @param isMultivalued If true, the list of all values of this attribute will be set as claim. If false, just first value will be set as claim.
     * @return This builder instance.
     */
    public ProtocolMapperBuilder isMultivalued(boolean isMultivalued) {
        this.isMultivalued = isMultivalued;
        return this;
    }

    /**
     * Name of the claim to insert into the token.
     * This can be a fully qualified name such as 'address.street'.
     * In this case, a nested JSON object is created.
     * To prevent nesting and to use the dot literally, escape the dot with a backslash (\.).
     * You can use the special token ${client_id}; it will be replaced by the actual client ID.
     *
     * @param tokenClaimName Mandatory name. An example usage is 'resource_access.${client_id}.roles'.
     *                       This option is especially useful when you add roles from all the clients, meaning 'Client ID' is disabled, and you want client roles of each client stored separately.
     * @return This builder instance.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public ProtocolMapperBuilder tokenClaimName(String tokenClaimName) throws IllegalArgumentException {
        if (tokenClaimName == null || tokenClaimName.isBlank()) {
            throw new IllegalArgumentException("tokenClaimName cannot be null or empty");
        }
        this.tokenClaimName = tokenClaimName;
        return this;
    }

    /**
     * Define the type fo JSON claim.
     * String by default.
     *
     * @param claimJSONType Mandatory type of JSON object (supported types: String, long, int, boolean, JSON).
     * @return This builder instance.
     * @throws IllegalArgumentException When mandatory parameter is missing or value is not supported.
     */
    public ProtocolMapperBuilder claimJSONType(String claimJSONType) throws IllegalArgumentException {
        if (claimJSONType == null || claimJSONType.isBlank()) {
            throw new IllegalArgumentException("claimJSONType cannot be null or empty");
        }
        // Check conformity of value according to supported
        Collection<String> supported = new ArrayList<>();
        supported.add("String");
        supported.add("long");
        supported.add("int");
        supported.add("boolean");
        supported.add("JSON");
        if (!supported.contains(claimJSONType)) {
            throw new IllegalArgumentException("claimJSONType not supported!");
        }
        this.claimJSONType = claimJSONType;
        return this;
    }

    /**
     * Should the claim be added to the ID token?
     * False by default.
     *
     * @param isAddedToIDToken True when shall be added.
     * @return This builder instance.
     */
    public ProtocolMapperBuilder isAddedToIDToken(boolean isAddedToIDToken) {
        this.isAddedToIDToken = isAddedToIDToken;
        return this;
    }

    /**
     * Should the claim be added to the access token?
     * False by default.
     *
     * @param isAddedToAccessToken True when shall be added.
     * @return This builder instance.
     */
    public ProtocolMapperBuilder isAddedToAccessToken(boolean isAddedToAccessToken) {
        this.isAddedToAccessToken = isAddedToAccessToken;
        return this;
    }

    /**
     * Should the claim be added to the lightweight access token?
     * False by default.
     *
     * @param isAddedToLightweightAccessToken True when shall be added.
     * @return This builder instance.
     */
    public ProtocolMapperBuilder isAddedToLightweightAccessToken(boolean isAddedToLightweightAccessToken) {
        this.isAddedToLightweightAccessToken = isAddedToLightweightAccessToken;
        return this;
    }

    /**
     * Should the claim be added to the userinfo?
     * False by default.
     *
     * @param isAddedToUserinfo True when shall be added.
     * @return This builder instance.
     */
    public ProtocolMapperBuilder isAddedToUserinfo(boolean isAddedToUserinfo) {
        this.isAddedToUserinfo = isAddedToUserinfo;
        return this;
    }

    /**
     * Should the claim be added to the token introspection?
     * False by default.
     *
     * @param isAddedToTokenIntrospection True when shall be added.
     * @return This builder instance.
     */
    public ProtocolMapperBuilder isAddedToTokenIntrospection(boolean isAddedToTokenIntrospection) {
        this.isAddedToTokenIntrospection = isAddedToTokenIntrospection;
        return this;
    }

    /**
     * Optional protocol supported by the prepared mapper.
     *
     * @param protocol Mandatory protocol (supported value: PROTOCOL_OPENIDCONNECT, PROTOCOL_SAML).
     * @return This builder instance.
     * @throws IllegalArgumentException When parameter is missing, or invalid.
     */
    public ProtocolMapperBuilder protocol(String protocol) throws IllegalArgumentException {
        if (protocol == null || protocol.isBlank()) {
            throw new IllegalArgumentException("protocol cannot be null or empty");
        }
        // Check supported value
        if (!PROTOCOL_OPENIDCONNECT.equals(protocol) && !PROTOCOL_SAML.equals(protocol)) {
            throw new IllegalArgumentException("invalid protocol: " + protocol);
        }
        this.protocol = protocol;
        return this;
    }

    /**
     * Get a container of attributes representing extended configuration.
     *
     * @return A set of attributes or null when none are defined.
     */
    public Map<String, String> config() {
        // Build instance of current values expected as protocol mapper resource attributes
        Map<String, String> attributes = new HashMap<>();

        /*"config": {
                    "introspection.token.claim": "true",
                    "multivalued": "true",
                    "userinfo.token.claim": "true",
                    "id.token.claim": "true",
                    "lightweight.claim": "false",
                    "access.token.claim": "true",
                    "claim.name": "resource_access.role",
                    "jsonType.label": "String",
                    "usermodel.clientRoleMapping.clientId": "web-reactive-frontend-system"
        }*/

        // Mandatory elements
        attributes.put(CONFIG_TOKEN_CLAIM_NAME, tokenClaimName);

        // optional values
        if (clientId != null && !clientId.isBlank())
            attributes.put(CONFIG_CLIENT_ID, clientId);
        if (isMultivalued != null)
            attributes.put(CONFIG_IS_MULTIVALUED, isMultivalued.toString());
        if (isAddedToIDToken != null)
            attributes.put(CONFIG_IS_CLAIM_ADDED_TO_TOKEN_ID, isAddedToIDToken.toString());
        if (isAddedToAccessToken != null)
            attributes.put(CONFIG_IS_ADDED_TO_ACCESS_TOKEN, isAddedToAccessToken.toString());
        if (isAddedToLightweightAccessToken != null)
            attributes.put(CONFIG_IS_ADDED_TO_LIGHTWEIGHT_ACCESS_TOKEN, isAddedToLightweightAccessToken.toString());
        if (isAddedToUserinfo != null)
            attributes.put(CONFIG_IS_ADDED_TO_USERINFO, isAddedToUserinfo.toString());
        if (isAddedToTokenIntrospection != null)
            attributes.put(CONFIG_IS_ADDED_TO_INTROSPECTION, isAddedToTokenIntrospection.toString());
        if (claimJSONType != null && !claimJSONType.isBlank())
            attributes.put(CONFIG_CLAIM_JSON_TYPE, claimJSONType);

        return attributes;
    }

}
