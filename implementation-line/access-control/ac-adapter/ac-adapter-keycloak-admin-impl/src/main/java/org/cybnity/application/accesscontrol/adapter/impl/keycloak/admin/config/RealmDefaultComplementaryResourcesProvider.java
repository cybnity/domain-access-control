package org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin.config;

import jakarta.ws.rs.core.Response;
import org.cybnity.accesscontrol.authorization.domain.model.PermissionCode;
import org.cybnity.application.accesscontrol.adapter.api.admin.OperationException;
import org.cybnity.keycloak.api.KeycloakAPIResponseCode;
import org.cybnity.keycloak.domain.model.*;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.*;

/**
 * Helper supporting the creation of complementary default resources required by a Tenant (Keycloak Realm) to operate in a CYBNITY context.
 * This class provided utility services including the knowledge of additional resources to create, to link with realm and/or to update with specific configuration elements.
 */
public class RealmDefaultComplementaryResourcesProvider {

    private final Keycloak keycloakClient;
    private final String tenantLabel;
    private final RealmWithDefaultExtendedResources defaultConfig;
    private final RealmRoleBuilder realmRoleBuilder;
    private final Map<String, String> configurationProperties;

    /**
     * Default constructor.
     *
     * @param keycloak                Mandatory operational client.
     * @param tenantLabel             Mandatory tenant identifier to enhance.
     * @param defaultConfig           Mandatory default configuration as provider of configuration elements.
     * @param realmRoleConfig         Mandatory role builder for realm.
     * @param configurationProperties Mandatory configuration strategy allowing read of default data usable for resources creations.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public RealmDefaultComplementaryResourcesProvider(Keycloak keycloak, String tenantLabel, RealmWithDefaultExtendedResources defaultConfig, RealmRoleBuilder realmRoleConfig, Map<String, String> configurationProperties) throws IllegalArgumentException {
        if (tenantLabel == null || tenantLabel.isEmpty()) {
            throw new IllegalArgumentException("Tenant label cannot be null or empty");
        }
        if (keycloak == null) {
            throw new IllegalArgumentException("Keycloak realm is null");
        }
        if (defaultConfig == null) {
            throw new IllegalArgumentException("Default realm config is null");
        }
        if (realmRoleConfig == null) {
            throw new IllegalArgumentException("Realm role config is null");
        }
        if (configurationProperties == null) {
            throw new IllegalArgumentException("configurationProperties is null");
        }
        this.keycloakClient = keycloak;
        this.tenantLabel = tenantLabel;
        this.defaultConfig = defaultConfig;
        this.realmRoleBuilder = realmRoleConfig;
        this.configurationProperties = configurationProperties;
    }


    /**
     * Create all complementary default resources required by th realm including:
     * - realm system clients custom roles (e.g; function roles)
     * - associated client roles with realm roles
     * - shared common client scopes between UI layer clients
     *
     * @throws OperationException When problem occurred during interactions with Keycloak server.
     */
    public void createResources() throws OperationException {
        try {
            // --- ADD OR CUSTOMIZE ANY REALM COMPLEMENTARY RESOURCE REQUIRED BY A CYBNITY TENANT FOR COLLABORATION WITH OTHER SYSTEMS ---
            RealmResource realm = this.keycloakClient.realm(tenantLabel);

            // --- REALM DEFAULT ROLES
            List<RoleRepresentation> defaultRealmRoles = this.tenantDefaultRealmRoles();
            for (RoleRepresentation realmRole : defaultRealmRoles) {
                realm.roles().create(realmRole);
            }
            List<RoleRepresentation> realmRolesRecords = new ArrayList<>(realm.roles().list()); // Including technical identifiers

            // --- CLIENT DEDICATED ROLES
            Map<ClientRepresentation, RoleRepresentation> defaultClientComplementaryRoleRecords = createSystemClientsRoles(realm, defaultConfig); // not yet associated to realm role
            // ----- ASSOCIATED DEFAULT CLIENTS ROLES TO DEFAULT REALM ROLE(S)
            associateClientRoles(realm, defaultClientComplementaryRoleRecords, realmRolesRecords);

            // --- REALM CLIENT SCOPES about shared configuration for UI layer systems (e.g; common shared roles for endpoint systems)
            Set<ClientRepresentation> eligibleToScopeAssignment = defaultClientComplementaryRoleRecords.keySet();
            List<ClientScopeRepresentation> clientScopes = createDefaultClientScopes(realm, defaultConfig, eligibleToScopeAssignment, realmRolesRecords);
            // ----- MAPPED DEFAULT CLIENTS TO COMMON SCOPES

            // --- REALM USERS
            //UsersResource defaultUsers = toEnhance.users();

            // --- REALM GROUPS

            // --- REALM SESSIONS

            // --- REALM EVENTS

            // --- REALM SETTINGS

            // --- REALM AUTHENTICATION

            // --- REALM IDENTITY PROVIDERS

            // --- REALM USER FEDERATION
        } catch (IllegalArgumentException iae) {
            // Development issue that shall never be occurred
            throw new OperationException(iae.getMessage());
        }
    }

    /**
     * Create association of specific client roles (e.g; allowing specific permissions or function role reserved to a client usage) to the transversal realm roles (e.g; hosting standard basic permissions).
     *
     * @param realm                           Mandatory current existing realm resource client (including technical identifier) that shall be extended in terms of settings into Keycloak instance.
     * @param defaultClientComplementaryRoles Client roles (including technical identifiers) eligible to mapping and association with one or multiple realm roles. No association created if null or empty.
     * @param realmDefaultRolesConfig         Realm roles (including technical identifiers) to map. No association created if null or empty.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     * @throws OperationException       When problem occurred during interactions with Keycloak server.
     */
    private void associateClientRoles(RealmResource realm, Map<ClientRepresentation, RoleRepresentation> defaultClientComplementaryRoles, List<RoleRepresentation> realmDefaultRolesConfig) throws IllegalArgumentException, OperationException {
        if (realm == null) throw new IllegalArgumentException("realm parameter is required!");
        if (defaultClientComplementaryRoles != null && realmDefaultRolesConfig != null) {
            // Identify the custom realm roles are existing and are eligible to association with client roles
            for (RoleRepresentation realmDefaultRole : realmDefaultRolesConfig) {
                List<RoleRepresentation> clientRolesEligibleToAssociation = new ArrayList<>();
                // Read client roles
                for (Map.Entry<ClientRepresentation, RoleRepresentation> entry : defaultClientComplementaryRoles.entrySet()) {
                    RoleRepresentation clientDedicatedRole = entry.getValue();
                    clientRolesEligibleToAssociation.add(clientDedicatedRole); // Add client role reference as eligible to association with realm role
                }

                // Get default realm role resource to update
                RoleResource realmRoleToAssociate = realm.roles().get(realmDefaultRole.getName()/* role name to associate with composites */);
                realmRoleToAssociate.addComposites(clientRolesEligibleToAssociation);
            }
        }
    }

    /**
     * Create role for clients with automatic association to existing realm modes.
     *
     * @param realm         Mandatory current existing realm resource client that shall be extended in terms of settings into Keycloak instance.
     * @param defaultConfig Mandatory default configuration including definition of default system clients potentially requiring creation of new roles with associated default realm roles.
     * @return Created clients roles including recorded technical identifiers (instance created by Keycloak) or empty set.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     * @throws OperationException       When problem occurred during interactions with Keycloak server.
     */
    private Map<ClientRepresentation, RoleRepresentation> createSystemClientsRoles(RealmResource realm, RealmWithDefaultExtendedResources defaultConfig) throws IllegalArgumentException, OperationException {
        if (realm == null) throw new IllegalArgumentException("realm parameter is required!");
        if (defaultConfig == null) throw new IllegalArgumentException("defaultConfig parameter is required!");
        Map<ClientRepresentation, RoleRepresentation> clientsRoles = new HashMap<>();
        try {
            // --- Identify default Clients supported by the realm (e.g; clients dedicated to each endpoint system requiring to be associated with realm default roles)
            ClientsResource clients = realm.clients(); // Identify all realm clients already existing in Keycloak
            String techId;
            for (ClientRepresentation client : defaultConfig.systemsClientConfigurationsSupported()) {
                List<ClientRepresentation> recordedClients = clients.findByClientId(client.getClientId()); // Search existing client record from logical client id
                for (ClientRepresentation c : recordedClients) { // Normally only one shall have been found
                    techId = c.getId(); // Read the client technical identifier usable for update of the record
                    if (techId != null && !techId.isBlank()) {
                        // Identified client technical id eligible to new role creation and default realm role association
                        for (RoleRepresentation role : defaultRolesRequiredByClientId(techId)) {// Identify from configuration
                            // Add role to client
                            clients.get(techId /* system client technical identifier */).roles().create(role);
                            // Report of created client role (including technical id recorded)
                            clientsRoles.put(c, clients.get(techId).roles().get(role.getName()).toRepresentation());
                        }
                    }
                }
            }
            return clientsRoles;
        } catch (Exception e) {
            // Keycloak interactions problem
            throw new OperationException(e);
        }
    }

    /**
     * Prepare a client scope and return built instance.
     *
     * @param defaultClientTypeAssignment None, Default or Optional. Client scope, which will be added as default addedScopes to each created client.
     * @param scopeName                   Mandatory client scope label.
     * @param scopeDescription            Optional scope description.
     * @param scopeProtocol               Mandatory SSO protocol configuration supplied by the client scope.
     * @param isDisplayOnConsentScreen    False by default.
     * @param consentScreenText           Optional Text that will be shown on the consent screen when this client scope is added to some client with consent required. Defaults to name of client scope if it is not filled.
     * @param isIncludedInTokenScope      False by default.
     * @param scopeDisplayOrder           Optional order of the provider in GUI (such as in Consent page).
     * @param mappers                     Optional mapper to build and to add into the client scope to prepare.
     * @return A prepared instance.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    private ClientScope prepareClientScope(String defaultClientTypeAssignment, String scopeName, String scopeDescription, String scopeProtocol, Boolean isDisplayOnConsentScreen, String consentScreenText, Boolean isIncludedInTokenScope, Integer scopeDisplayOrder, Collection<ProtocolMapperBuilder> mappers) throws IllegalArgumentException {
        ClientScopeBuilder builder = new ClientScopeBuilder();

        // Define client scope unique name defined into a realm (according to naming convention based on shared "type" between multiple clients)
        if (scopeName == null || scopeName.isBlank())
            throw new IllegalArgumentException("scopeName parameter is required!");

        builder.name(scopeName);
        builder.description(scopeDescription);
        builder.type(defaultClientTypeAssignment);
        if (scopeProtocol == null || scopeProtocol.isBlank())
            throw new IllegalArgumentException("scopeProtocol parameter is required!");

        builder.protocol(scopeProtocol);
        builder.displayOnConsentScreen(isDisplayOnConsentScreen);
        builder.consentScreenText(consentScreenText);
        builder.includedInTokenScope(isIncludedInTokenScope);
        builder.displayOrder(scopeDisplayOrder);

        // --- DEFINE THE MAPPERS REQUIRED FOR SHARING OF SCOPE TO THE ELIGIBLE CLIENTS
        if (mappers != null) {
            for (ProtocolMapperBuilder mapper : mappers) {
                builder.addProtocolMapper(mapper);// Include into the build of client scope as auto-assigned
            }
        }

        return builder.build();
    }

    /**
     * Create default clients scopes that are a common set of protocol mappers and roles that are shared between multiple clients.
     * <p>
     * Usage: if there are many applications to secure and register within the organization (e.g; multi tenant), it can become tedious to configure role scope mappings for each of these systems' clients. Keycloak allows to define a shared client configuration in an entity called a client scope. To get client roles as a custom key in the JWT token, add client scope to put client roles in access token.
     * Client scopes naming convention: the "type" based naming template is applied for definition of each client scope name.
     * This method create the default scopes usable into a realm.
     *
     * @param realm                             Mandatory current existing realm resource client (including technical identifier).
     * @param defaultConfig                     Mandatory default configuration including definition of default scope potentially requiring creation for common roles sharing with associated default clients.
     * @param eligibleToScopeAssignment         Clients (including technical identifiers) eligible to subject of mapping with default clients scopes. No scope created if null or empty.
     * @param eligibleToClientScopesAssociation Optional roles to associate to default clients scopes. If there is no role scope mapping defined per client scope, each user is permitted to use those client scopes.
     *                                          If there are role scope mappings defined (based on the eligible roles for association), the user must be a member of at least one of the roles.
     * @return Created clients scopes including recorded technical identifiers (instance created by Keycloak) or empty set.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     * @throws OperationException       When problem occurred during interactions with Keycloak server.
     */
    private List<ClientScopeRepresentation> createDefaultClientScopes(RealmResource realm, RealmWithDefaultExtendedResources defaultConfig, Set<ClientRepresentation> eligibleToScopeAssignment, List<RoleRepresentation> eligibleToClientScopesAssociation) throws IllegalArgumentException, OperationException {
        if (realm == null) throw new IllegalArgumentException("realm parameter is required!");
        if (defaultConfig == null) throw new IllegalArgumentException("defaultConfig parameter is required!");
        List<ClientScopeRepresentation> addedScopes = new ArrayList<>();
        if (eligibleToScopeAssignment != null) {
            try {
                ClientScopesResource scopes = realm.clientScopes(); // Current existing scope of the realm context

                // --- DEFAULT CLIENT SCOPES PREPARATION AND RECORDING INTO KEYCLOAK
                // Create each common scope required by default for the realm
                List<ClientScope> defaultPreparedClientScopes = new ArrayList<>();

                int clientScopeCount = 1;
                boolean findNextClientScopeDefaultConfig = true;
                while (findNextClientScopeDefaultConfig) {
                    try {
                        // Attempt to build next existing configuration about client scope  to add as default into Keycloak
                        String clientScopeConfigPropertyName = "REALM_DEFAULT_CLIENTSCOPE_";
                        // Define client scope unique name defined into a realm (according to naming convention based on shared "type" between multiple clients)
                        String clientScopeDefaultPropertyID = clientScopeConfigPropertyName + clientScopeCount;
                        String defaultClientTypeAssignment = ClientScopeBuilder.TYPE_DEFAULT; // None, Default or Optional. Client scope, which will be added as default addedScopes to each created client
                        final String scopeName = configurationProperties.get(clientScopeDefaultPropertyID + "_NAME");
                        Collection<ProtocolMapperBuilder> mappers = new ArrayList<>();

                        // --- DEFINE THE MAPPERS REQUIRED FOR SHARING OF SCOPE TO THE ELIGIBLE CLIENTS
                        // See <a href="https://www.keycloak.org/admin-api/protocol-mappers">protocol mappers available by default into Keycloak</a>.
                        // ----- web frontend (User Client Role mapper)

                        int clientMapperCount = 1;
                        boolean findNextMapperConfig = true;
                        String mapperName;
                        while (findNextMapperConfig) {
                            try {
                                // Attempt to build next existing configuration about mapper to add for client scope
                                mapperName = configurationProperties.getOrDefault(clientScopeDefaultPropertyID + "_MAPPER_" + clientMapperCount + "_NAME", null);

                                if (mapperName != null && !mapperName.isBlank()) {
                                    mappers.add(new ProtocolMapperBuilder()
                                            .mapperName(mapperName)
                                            .protocol(ClientScopeBuilder.PROTOCOL_OPENIDCONNECT)
                                            .clientId(configurationProperties.get(clientScopeDefaultPropertyID + "_MAPPER_" + clientMapperCount + "_CLIENTID"))
                                            .isMultivalued(Boolean.parseBoolean(configurationProperties.get(clientScopeDefaultPropertyID + "_MAPPER_" + clientMapperCount + "_IS_MULTIVALUED")))
                                            .tokenClaimName(configurationProperties.get(clientScopeDefaultPropertyID + "_MAPPER_" + clientMapperCount + "_TOKEN_CLAIM_NAME"))
                                            .claimJSONType(configurationProperties.get(clientScopeDefaultPropertyID + "_MAPPER_" + clientMapperCount + "_CLAIM_JSON_TYPE"))
                                            .isAddedToIDToken(Boolean.parseBoolean(configurationProperties.get(clientScopeDefaultPropertyID + "_MAPPER_" + clientMapperCount + "_IS_ADDED_TO_ID_TOKEN")))
                                            .isAddedToAccessToken(Boolean.parseBoolean(configurationProperties.get(clientScopeDefaultPropertyID + "_MAPPER_" + clientMapperCount + "_IS_ADDED_TO_ACCESS_TOKEN")))
                                            .isAddedToUserinfo(Boolean.parseBoolean(configurationProperties.get(clientScopeDefaultPropertyID + "_MAPPER_" + clientMapperCount + "_IS_ADDED_TO_USER_INFO")))
                                            .isAddedToLightweightAccessToken(Boolean.parseBoolean(configurationProperties.get(clientScopeDefaultPropertyID + "_MAPPER_" + clientMapperCount + "_IS_ADDED_TO_LIGHT_WEIGHT_ACCESS_TOKEN")))
                                            .isAddedToTokenIntrospection(Boolean.parseBoolean(configurationProperties.get(clientScopeDefaultPropertyID + "_MAPPER_" + clientMapperCount + "_IS_ADDED_TO_TOKEN_INTROSPECTION")))
                                            .mapperTypeReferenceName(configurationProperties.get(clientScopeDefaultPropertyID + "_MAPPER_" + clientMapperCount + "_MAPPER_TYPE_REFERENCE_NAME")));
                                } else {
                                    findNextMapperConfig = false; // Stop configuration file read (cause none existing new indexed mapper definition
                                }
                            } catch (Exception e) {
                                // Stop read of configuration because none additional mapper is defined
                                findNextMapperConfig = false;
                            } finally {
                                clientMapperCount++;
                            }
                        }

                        defaultPreparedClientScopes.add(prepareClientScope(defaultClientTypeAssignment,
                                scopeName,
                                configurationProperties.get(clientScopeDefaultPropertyID + "_DESCRIPTION"),
                                ClientScopeBuilder.PROTOCOL_OPENIDCONNECT,
                                Boolean.parseBoolean(configurationProperties.get(clientScopeDefaultPropertyID + "_DISPLAY_ON_CONSENT_SCREEN")),
                                configurationProperties.get(clientScopeDefaultPropertyID + "_CONSENT_SCREEN_TEXT"),
                                Boolean.parseBoolean(configurationProperties.get(clientScopeDefaultPropertyID + "_IS_INCLUDED_IN_TOKEN_SCOPE")),
                                Integer.parseInt(configurationProperties.get(clientScopeDefaultPropertyID + "_DISPLAY_ORDER")),
                                mappers)); // Build the client scope to add into Keycloak

                        // ----- ADD OTHER SYSTEM MAPPER ACCORDING TO SAME OR DEDICATED CLIENTS AND SCOPE

                        // Record prepared scopes into Keycloak as new client scope
                        for (ClientScope aPreparedScope : defaultPreparedClientScopes) {
                            try (Response resp = scopes.create(aPreparedScope)) {
                                if (!KeycloakAPIResponseCode.CREATED.responseCode().equals(Integer.valueOf(resp.getStatus()).toString())) {
                                    // Rejection for cause of conflict (409) ar forbidden (403) creation
                                    throw new OperationException(resp.toString());
                                }

                                // --- UPDATE CLIENTS ELIGIBLE TO DEFAULT OR OPTIONAL SCOPE
                                // Update all clients eligible to new scope as default or optional scope
                                String realmName = realm.toRepresentation().getRealm();
                                realm.clientScopes().findAll().stream()
                                        .filter(scope -> scopeName.equals(scope.getName()) /* select only the new scope eligible for add as default or optional on realm existing clients */)
                                        .forEach(clientScopeRepresentation -> {
                                            // Add complementary configuration elements to each client scope

                                            // --- ADD CLIENT SCOPE TO CLIENT
                                            for (ClientRepresentation client : eligibleToScopeAssignment) {
                                                // Assign the client scope on each default client according to its type (default, or optional)
                                                ClientResource clientRef = realm.clients().get(client.getId()); // Get the client eligible to be set on the new scope
                                                boolean isScopeAddedToClient = false;
                                                if (ClientScopeBuilder.TYPE_DEFAULT.equals(defaultClientTypeAssignment)) {
                                                    // Add scope to the existing default scopes
                                                    clientRef.addDefaultClientScope(clientScopeRepresentation.getId());
                                                    isScopeAddedToClient = true;
                                                } else if (ClientScopeBuilder.TYPE_OPTIONAL.equals(defaultClientTypeAssignment)) {
                                                    // Add scope to the existing optional scopes
                                                    clientRef.addOptionalClientScope(clientScopeRepresentation.getId());
                                                    isScopeAddedToClient = true;
                                                }

                                                if (eligibleToClientScopesAssociation != null) {
                                                    // --- ADD LINK TO DEFAULT ROLE (AS SCOPE MAPPING) TO CLIENT SCOPE
                                                    // Define client-level or realm roles to associate with the client-scope according their type
                                                    List<RoleRepresentation> clientRolesToAssociate = new ArrayList<>();
                                                    List<RoleRepresentation> realmRolesToAssociate = new ArrayList<>();

                                                    for (RoleRepresentation roleToAssociate : eligibleToClientScopesAssociation) {
                                                        if (roleToAssociate.getClientRole()) {
                                                            // Identify the client roles to associate for permitting only users to use the client scope (relevant of this client role)
                                                            clientRolesToAssociate.add(roleToAssociate);
                                                        } else {
                                                            // Identify the realm roles to associate for permitting only users to use the client scope (relevant of this realm role)
                                                            realmRolesToAssociate.add(roleToAssociate);
                                                        }
                                                    }

                                                    if (!clientRolesToAssociate.isEmpty() || !realmRolesToAssociate.isEmpty()) {
                                                        RoleMappingResource roleMap = realm.clientScopes().get(clientScopeRepresentation.getId()).getScopeMappings();
                                                        if (!clientRolesToAssociate.isEmpty()) {
                                                            // Create scope mapping between client and role (entry into client scope mappings)
                                                            roleMap.clientLevel(client.getId() /* UUID internal client identification id as consistent reference to the client managed by Keycloak */).add(clientRolesToAssociate);
                                                        }
                                                        if (!realmRolesToAssociate.isEmpty()) {
                                                            // Create scope mapping between client scope and role (entry into scope mappings)
                                                            roleMap.realmLevel().add(realmRolesToAssociate);
                                                            // TODO add new assigned scope "use-tenant" role to "ui-layer-system-roles" client scope
                                                        }
                                                    }
                                                }
                                                if (isScopeAddedToClient)
                                                    addedScopes.add(clientScopeRepresentation);
                                            }
                                        });
                            }
                        }
                    } catch (Exception e) {
                        // Stop read of configuration because none additional mapper is defined
                        findNextClientScopeDefaultConfig = false;
                    } finally {
                        clientScopeCount++;
                    }
                }


                // Todo use RealmWithDefaultExtendedResources defaultConfig for read of default addedScopes to prepare (apply same approach that createSystemClientsRoles() )


                // Mapper type: label
                // Mapper name: unique mapper name
                // Role attribute name: Name of the SAML attribute you want to put your roles into. i.e. 'Role', 'memberOf'.
                // Friendly name: Standard SAML attribute setting. An optional, more human-readable form of the attribute's name that can be provided if the actual attribute name is cryptic.
                // SAML attribute name format: Basic // SAML Attribute NameFormat. Can be basic, URI reference, or unspecified.
                // Single Role Attribute: on/off // If true, all roles will be stored under one attribute with multiple attribute values.
            } catch (Exception e) {
                throw new OperationException(e);
            }
        }
        return addedScopes; // instance of created scope including technical identifiers
    }

    /**
     * Identify default roles that are required by a system client according to its client identifier.
     * Best practice: specific function based roles can be created by client (role by function with naming convention based on template <<function category name>>-<<responsibility label>>).
     *
     * @param clientId Client identifier. Client id is its logical name to find into the configuration that is defining what Client roles need to be created by default.
     * @return A list of roles or empty list.
     */
    private List<RoleRepresentation> defaultRolesRequiredByClientId(String clientId) {
        List<RoleRepresentation> roles = new ArrayList<>();
        if (clientId != null && !clientId.isBlank()) {
            RoleBuilder roleBuilder = new RoleBuilder();

            // Prepare each default role dedicated to client area (each role is usable only in the client scope if not associated to a realm role)
            roles.add(roleBuilder.name(PermissionCode.ACCESS_APPLICATIONS.label())
                    .description(configurationProperties.get("REALM_DEFAULT_ROLE_" + PermissionCode.ACCESS_APPLICATIONS.name()))
                    .isClientRole(Boolean.TRUE) // role usable into the client scope
                    .isComposite(Boolean.FALSE) // Originally not associated to realm transversal role
                    .build());

            // Add eventual other role dedicated to same client...
        }
        return roles; // return eligible as default roles
    }

    /**
     * Prepare a list of default roles that are required for a realm usage.
     * For example, the roles assigned by default to any type of user and-or system roles (e.g; dedicated to environment or system types) required by CYBNITY application modules to use Keycloak authorization for access to specific resources.
     *
     * @return A list of transversal and default roles (e.g; "use-tenant" role) assignable to a realm, or empty list.
     */
    public List<RoleRepresentation> tenantDefaultRealmRoles() {
        List<RoleRepresentation> roles = new ArrayList<>();
        // doc: https://github.com/cybnity/domain-access-control/blob/feature-237/implementation-line/access-control/ac-domain-model/domain-model-components.md

        // Define basic role regarding any type of user authorized to use a tenant perimeter (equals to a realm scope)
        roles.add(realmRoleBuilder
                .name(Sanitizer.removeAllBlankCharacters(PermissionCode.USE_TENANT.label()))
                .isClientRole(Boolean.FALSE)
                .isComposite(Boolean.FALSE)
                .description(configurationProperties.get("REALM_DEFAULT_ROLE_" + PermissionCode.USE_TENANT.name()))
                .build());

        // Add eventual other role dedicated to each tenant...
        return roles; // return eligible as default roles
    }

}
