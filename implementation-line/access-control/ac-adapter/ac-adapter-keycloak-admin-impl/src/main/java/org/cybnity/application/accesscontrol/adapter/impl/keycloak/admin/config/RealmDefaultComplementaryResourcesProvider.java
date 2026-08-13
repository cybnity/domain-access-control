package org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin.config;

import jakarta.ws.rs.core.Response;
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
            List<ClientScopeRepresentation> clientScopes = createDefaultClientsScopes(realm, defaultConfig, eligibleToScopeAssignment);
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
     * Create default clients scopes that are a common set of protocol mappers and roles that are shared between multiple clients.
     * <p>
     * Usage: if there are many applications to secure and register within the organization (e.g; multi tenant), it can become tedious to configure role scope mappings for each of these systems' clients. Keycloak allows to define a shared client configuration in an entity called a client scope. To get client roles as a custom key in the JWT token, add client scope to put client roles in access token.
     * Client scopes naming convention: the "type" based naming template is applied for definition of each client scope name.
     * This method create the default scopes usable into a realm.
     *
     * @param realm                     Mandatory current existing realm resource client (including technical identifier).
     * @param defaultConfig             Mandatory default configuration including definition of default scope potentially requiring creation for common roles sharing with associated default clients.
     * @param eligibleToScopeAssignment Clients (including technical identifiers) eligible to subject of mapping with default clients scopes. No scope created if null or empty.
     * @return Created clients scopes including recorded technical identifiers (instance created by Keycloak) or empty set.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     * @throws OperationException       When problem occurred during interactions with Keycloak server.
     */
    private List<ClientScopeRepresentation> createDefaultClientsScopes(RealmResource realm, RealmWithDefaultExtendedResources defaultConfig, Set<ClientRepresentation> eligibleToScopeAssignment) throws IllegalArgumentException, OperationException {
        if (realm == null) throw new IllegalArgumentException("realm parameter is required!");
        if (defaultConfig == null) throw new IllegalArgumentException("defaultConfig parameter is required!");
        List<ClientScopeRepresentation> addedScopes = new ArrayList<>();
        if (eligibleToScopeAssignment != null) {
            try {
                ClientScopesResource scopes = realm.clientScopes(); // Current existing scope of the realm context

                // --- DEFAULT CLIENT SCOPES PREPARATION AND RECORDING INTO KEYCLOAK
                // Create each common scope required by default for the realm
                ClientScopeBuilder builder = new ClientScopeBuilder();

                String defaultClientTypeAssignment = ClientScopeBuilder.TYPE_DEFAULT; // None, Default or Optional. Client scope, which will be added as default addedScopes to each created client

                // TODO replace all static values by values coming from profile and config file

                // Define client scope unique name defined into a realm (according to naming convention based on shared "type" between multiple clients)
                String scopeName = "ui-layer-systems-roles";
                builder.name(scopeName); // <<system type>>-<<system label>>-<<custom logical name>> naming convention
                builder.description("OpenID Connect build-in scope about the common roles of clients used by systems of UI layer");
                builder.type(defaultClientTypeAssignment);
                builder.protocol(ClientScopeBuilder.PROTOCOL_OPENIDCONNECT);// SSO protocol configuration supplied by the client scope
                builder.displayOnConsentScreen(true);
                builder.consentScreenText("bonjour consents"); // Text that will be shown on the consent screen when this client scope is added to some client with consent required. Defaults to name of client scope if it is not filled.
                builder.includedInTokenScope(true);
                builder.displayOrder(1);// Specify order of the provider in GUI (such as in Consent page)

                // --- DEFINE THE MAPPERS REQUIRED FOR SHARING OF SCOPE TO THE ELIGIBLE CLIENTS
                // ----- web frontend system mapper
                ProtocolMapperBuilder protocolMapperBuilder = new ProtocolMapperBuilder()
                        .mapperName("ui-clients-role")
                        .protocol(ClientScopeBuilder.PROTOCOL_OPENIDCONNECT)
                        .clientId("web-reactive-frontend-system")
                        .isMultivalued(true)
                        .tokenClaimName("client.role")
                        .claimJSONType("String")
                        .isAddedToIDToken(true)
                        .isAddedToAccessToken(true)
                        .isAddedToUserinfo(true)
                        .isAddedToLightweightAccessToken(false)
                        .isAddedToTokenIntrospection(true)
                        .mapperTypeReferenceName("oidc-usermodel-client-role-mapper"); // See <a href="https://www.keycloak.org/admin-api/protocol-mappers">protocol mappers available by default into Keycloak</a>.
                builder.protocolMapper(protocolMapperBuilder);// Include into the build of client scope as auto-assigned
                ClientScope aPreparedScope = builder.build(); // Build the client scope to add into Keycloak

                // ----- ADD OTHER SYSTEM MAPPER ACCORDING TO SAME OR DEDICATED CLIENTS AND SCOPE

                // ----- user realm role mapper
                // TODO create here additional common User Realm Role

                try (Response resp = scopes.create(aPreparedScope)) {
                    if (!KeycloakAPIResponseCode.CREATED.responseCode().equals(Integer.valueOf(resp.getStatus()).toString())) {
                        // Rejection for cause of conflict (409) ar forbidden (403) creation
                        throw new OperationException(resp.toString());
                    }

                    // --- UPDATE CLIENTS ELIGIBLE TO DEFAULT OR OPTIONAL SCOPE
                    // Update all clients eligible to new scope as default or optional scope
                    realm.clientScopes().findAll().stream()
                            .filter(scope -> scopeName.equals(scope.getName()) /* select only the new scope eligible for add as default or optional on realm existing clients */)
                            .forEach(clientScopeRepresentation -> {
                                // Assign the client scope on each default client according to its type (default, or optional)
                                for (ClientRepresentation client : eligibleToScopeAssignment) {
                                    ClientResource clientRef = realm.clients().get(client.getId()); // Get the client eligible to be set on the new scope
                                    if (ClientScopeBuilder.TYPE_DEFAULT.equals(defaultClientTypeAssignment)) {
                                        // Add scope to the existing default scopes
                                        clientRef.addDefaultClientScope(clientScopeRepresentation.getId());
                                        addedScopes.add(clientScopeRepresentation);
                                    } else if (ClientScopeBuilder.TYPE_OPTIONAL.equals(defaultClientTypeAssignment)) {
                                        // Add scope to the existing optional scopes
                                        clientRef.addOptionalClientScope(clientScopeRepresentation.getId());
                                        addedScopes.add(clientScopeRepresentation);
                                    }
                                }
                            });
                }


                // TODO change static value for environment variable values relative to minimum set of client addedScopes to create for the clients (equals to name of a client)
                // TODO The envt variables should define a common client scope assigning shared roles (over mappers) for backend and frontend clients (systems using clients from the UI layer)


                // Identify the default client scope to prepare and to add into Keycloak for the realm
                // Todo use RealmWithDefaultExtendedResources defaultConfig for read of default addedScopes to prepare (apply same approach that createSystemClientsRoles() )


                // Mapper type: label
                // Mapper name: unique mapper name
                // Role attribute name: Name of the SAML attribute you want to put your roles into. i.e. 'Role', 'memberOf'.
                // Friendly name: Standard SAML attribute setting. An optional, more human-readable form of the attribute's name that can be provided if the actual attribute name is cryptic.
                // SAML attribute nameformat: Basic // SAML Attribute NameFormat. Can be basic, URI reference, or unspecified.
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
            // Search from configuration which role shall be created for the identifiable client
            // TODO change static value for environment variable values relative to minimum set of roles to create for the clientId (equals to name of a client)
            // TODO The envt variables should define a role for backend client, and another one for frontend client (aligned with existing endpoints created via configuration)

            // Prepare each default role dedicated to client area (each role is usable only in the client scope if not associated to a realm role)
            RoleBuilder roleBuilder = new RoleBuilder();
            roles.add(roleBuilder.name("access-applications")
                    .description("Standard role for access to exposed CYBNITY application (e.g; frontend user interface, or backend API)")
                    .isClientRole(Boolean.TRUE) // role usable into the client scope
                    .isComposite(Boolean.FALSE) // Originally not associated to realm transversal role
                    .build());

            // Add eventual other role dedicated to client
        }
        return roles;// return eligible as default role
    }

    /**
     * Prepare a list of default roles that are required for a realm usage.
     * For example, the roles assigned by default to any type of user and-or system roles (e.g; dedicated to environment or system types) required by CYBNITY application modules to use Keycloak authorization for access to specific resources.
     *
     * @return A list of transversal and default roles (e.g; "use-tenant" role) assignable to a realm, or empty list.
     */
    public List<RoleRepresentation> tenantDefaultRealmRoles() {
        List<RoleRepresentation> roles = new ArrayList<>();
        // TODO Change static roles definitions required by CYBNITY application and UI layers, for read from envt variables
        // doc: https://github.com/cybnity/domain-access-control/blob/feature-237/implementation-line/access-control/ac-domain-model/domain-model-components.md

        // Define basic role regarding any type of user authorized to use a tenant perimeter (equals to a realm scope)
        roles.add(realmRoleBuilder
                .name(Sanitizer.removeAllBlankCharacters("use-tenant"))
                .isClientRole(Boolean.FALSE)
                .isComposite(Boolean.FALSE)
                .description("Function based role regarding authorized access and use of resources under realm ownership")
                .build());
        return roles;
    }

}
