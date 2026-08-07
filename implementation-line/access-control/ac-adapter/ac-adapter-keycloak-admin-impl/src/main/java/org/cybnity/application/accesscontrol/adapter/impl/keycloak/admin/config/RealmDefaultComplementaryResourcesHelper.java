package org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin.config;

import org.cybnity.application.accesscontrol.adapter.api.admin.OperationException;
import org.cybnity.keycloak.domain.model.RealmRoleBuilder;
import org.cybnity.keycloak.domain.model.RealmWithDefaultExtendedResources;
import org.cybnity.keycloak.domain.model.RoleBuilder;
import org.cybnity.keycloak.domain.model.Sanitizer;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper supporting the creation of complementary default resources required by a Tenant (Keycloak Realm) to operate in a CYBNITY context.
 * This class provided utility services including the knowledge of additional resources to create, to link with realm and/or to update with specific configuration elements.
 */
public class RealmDefaultComplementaryResourcesHelper {

    private final Keycloak keycloakClient;
    private final String tenantLabel;
    private final RealmWithDefaultExtendedResources defaultConfig;
    private final RealmRoleBuilder realmRoleBuilder;

    /**
     * Default constructor.
     *
     * @param keycloak        Mandatory operational client.
     * @param tenantLabel     Mandatory tenant identifier to enhance.
     * @param defaultConfig   Mandatory default configuration as provider of configuration elements.
     * @param realmRoleConfig Mandatory role builder for realm.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public RealmDefaultComplementaryResourcesHelper(Keycloak keycloak, String tenantLabel, RealmWithDefaultExtendedResources defaultConfig, RealmRoleBuilder realmRoleConfig) throws IllegalArgumentException {
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
        this.keycloakClient = keycloak;
        this.tenantLabel = tenantLabel;
        this.defaultConfig = defaultConfig;
        this.realmRoleBuilder = realmRoleConfig;
    }


    /**
     * Create all complementary default resources required by th realm including:
     * - realm system clients custom roles (e.g; function roles)
     * - associated client roles with realm roles
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

            // ----- CLIENT DEDICATED ROLES
            Map<ClientRepresentation, RoleRepresentation> defaultClientComplementaryRoleRecords = createSystemClientsRoles(realm, defaultConfig); // not yet associated to realm role

            // ----- ASSOCIATED DEFAULT CLIENTS ROLES TO DEFAULT REALM ROLE(S)
            associateClientRoles(realm, defaultClientComplementaryRoleRecords, realmRolesRecords);

            // --- REALM CLIENT SCOPES supported


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
                    ClientRepresentation client = entry.getKey();
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
        // TODO Chante static roles definitions required by CYBNITY application and UI layers, for read from envt variables
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
