package org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin.config;

import org.cybnity.application.accesscontrol.adapter.api.admin.OperationException;
import org.cybnity.keycloak.domain.model.RealmWithDefaultExtendedResources;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper supporting the creation of complementary default resources required by a Tenant (Keycloak Realm) to operate in a CYBNITY context.
 * This class provided utility services including the knowledge of additional resources to create, to link with realm and/or to update with specific configuration elements.
 */
public class RealmDefaultComplementaryResourcesHelper {

    private final Keycloak keycloakClient;
    private final String tenantLabel;
    private final RealmWithDefaultExtendedResources defaultConfig;

    /**
     * Default constructor.
     *
     * @param keycloak      Mandatory operational client.
     * @param tenantLabel   Mandatory tenant identifier to enhance.
     * @param defaultConfig Mandatory default configuration as provider of configuration elements.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public RealmDefaultComplementaryResourcesHelper(Keycloak keycloak, String tenantLabel, RealmWithDefaultExtendedResources defaultConfig) throws IllegalArgumentException {
        if (tenantLabel == null || tenantLabel.isEmpty()) {
            throw new IllegalArgumentException("Tenant label cannot be null or empty");
        }
        if (keycloak == null) {
            throw new IllegalArgumentException("Keycloak realm is null");
        }
        if (defaultConfig == null) {
            throw new IllegalArgumentException("Default realm config is null");
        }
        this.keycloakClient = keycloak;
        this.tenantLabel = tenantLabel;
        this.defaultConfig = defaultConfig;
    }


    /**
     * Create all complementary default resources required by th realm including:
     * - realm system clients default roles associated to realm roles
     *
     * @throws OperationException When problem occurred during interactions with Keycloak server.
     */
    public void createResources() throws OperationException {
        try {
            // --- ADD OR CUSTOMIZE ANY REALM COMPLEMENTARY RESOURCE REQUIRED BY A CYBNITY TENANT FOR COLLABORATION WITH OTHER SYSTEMS ---

            // ----- CLIENT DEFAULT ROLES
            createSystemClientsRoles(this.keycloakClient.realm(tenantLabel), defaultConfig);


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
     * Create role for clients with automatic association to existing realm modes.
     *
     * @param realm         Mandatory current existing realm resource client (accessor to Keycloak) that shall be extended in terms of settings into Keycloak instance.
     * @param defaultConfig Mandatory default configuration including definition of default system clients potentially requiring creation of new roles with associated default realm roles.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     * @throws OperationException       When problem occurred during interactions with Keycloak server.
     */
    private void createSystemClientsRoles(RealmResource realm, RealmWithDefaultExtendedResources defaultConfig) throws IllegalArgumentException, OperationException {
        if (realm == null) throw new IllegalArgumentException("realm parameter is required!");
        if (defaultConfig == null) throw new IllegalArgumentException("defaultConfig parameter is required!");
        try {
            // --- Identify default Clients supported by the realm (e.g; clients dedicated to each endpoint system requiring to be associated with realm default roles)
            List<String> eligibleToRealmRoleAssociationClientIDs = new ArrayList<>();
            for (ClientRepresentation client : defaultConfig.systemsClientConfigurationsSupported()) {
                eligibleToRealmRoleAssociationClientIDs.add(client.getClientId()); // Identified client id eligible to new role creation and default realm role association
            }
            if (!eligibleToRealmRoleAssociationClientIDs.isEmpty()) {
                ClientsResource clients = realm.clients(); // identify all realm clients existing
                List<RoleRepresentation> rolesToAssociate;
                for (String clientId : eligibleToRealmRoleAssociationClientIDs) {
                    rolesToAssociate = defaultRolesRequiredByClientId(clientId);
                    for (RoleRepresentation role : rolesToAssociate) {
                        // Create client role with association to realm role
                        clients.get(clientId /* system client identifier */).roles().create(role);
                    }
                }
            }

        } catch (Exception e) {
            // Keycloak interactions problem
            throw new OperationException(e);
        }
    }

    /**
     * Identify default roles that are required by a system client.
     *
     * @param clientId Client identifier.
     * @return A list of roles or empty list.
     */
    private List<RoleRepresentation> defaultRolesRequiredByClientId(String clientId) {
        List<RoleRepresentation> roles = new ArrayList<>();
        if (clientId != null && !clientId.isBlank()) {
            // Search from configuration which role shall be created for the identifiable client
            // TODO change static value for environment variable values relative to minimum set of roles per clientId (equals to name of clients)

            // Prepare new role dedicated to client
            RoleRepresentation role = new RoleRepresentation();
            role.setName("endpoint-ui-user");
            role.setDescription("Standard user role of the an endpoint exposed by CYBNITY application (e.g; frontend user interface, or backend API)");
            role.setComposite(true);
            role.setClientRole(true);
            role.setContainerId(clientId);
            roles.add(role); // return as default role

        }
        return roles;
    }
}
