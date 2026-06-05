package org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin;

import org.cybnity.application.accesscontrol.adapter.api.admin.IAccessAdminAdapter;
import org.cybnity.application.accesscontrol.adapter.api.admin.OperationException;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.model.Tenant;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ServerInfoResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;

import java.util.logging.Logger;

/**
 * Contract relative to access capabilities administration (e.g setting of system's client scopes, access control configuration supervision).
 * For example, services allowing realms, access workflows, standardized roles management according to privileged capabilities.
 * The features supported by this adapter implementation class are focused on administration of User Access Management's elements.
 */
public class AccessAdminAdapterKeycloakImpl implements IAccessAdminAdapter {

    /**
     * Technical logging
     */
    private static final Logger logger = Logger.getLogger(AccessAdminAdapterKeycloakImpl.class.getName());
    /**
     * Current context of adapter runtime.
     */
    private final IContext context;
    /**
     * Utility class managing the verification of operable adapter instance.
     */
    private ExecutableAdminAdapterChecker healthyChecker;

    /**
     * Keycloak instance Administration REST api.
     * Single of connector to Keycloak server (default master realm).
     * See features exposed by the Admin API at https://www.keycloak.org/docs-api/latest/rest-api/index.html
     */
    private Keycloak keycloakAdminClient;

    /**
     * Keycloak default master realm name as configured by Kyecolack server.
     * By default based on environment variable read.
     */
    private String masterRealmName;

    /**
     * Default constructor of the adapter ready to manage remote interactions with a Keycloak instance(s).
     *
     * @param context Mandatory context provider of reusable configuration allowing
     *                to connect to instance(s). Shall include settings (e.g. http url and port, master realm name, admin account and password) allowing to connect Keycloak over administration API.
     * @throws IllegalArgumentException    When mandatory parameter is missing.
     * @throws UnoperationalStateException When any required environment variable is
     *                                     not defined or have not value ready for
     *                                     use.
     */
    public AccessAdminAdapterKeycloakImpl(IContext context) throws IllegalArgumentException, UnoperationalStateException {
        if (context == null)
            throw new IllegalArgumentException("Context parameter is required!");
        this.context = context;

        // Check the minimum required data allowing connection to the targeted Keycloak
        // server
        checkHealthyState();
    }

    @Override
    public void freeUpResources() {
        // Disconnect Keycloak REST API stub is existing
        if (keycloakAdminClient != null) keycloakAdminClient.close();
    }

    @Override
    public void checkHealthyState() throws UnoperationalStateException {
        if (healthyChecker == null)
            healthyChecker = new ExecutableAdminAdapterChecker(context);
        // Execution the health check about configuration
        healthyChecker.checkOperableState();

        // Test connection to Keycloak instance over the Admin REST API client library
        // See https://www.keycloak.org/securing-apps/admin-client documentation about Keycloak admin client usage
        if (getKeycloakAdminClient(context).isClosed())
            throw new UnoperationalStateException("Keycloak API is closed and is not usable!");
        try {
            RealmResource realmResource = keycloakAdminClient.realm("master"); // TODO change for default master from context environment variable
            RealmRepresentation realm = realmResource.toRepresentation();
        } catch (Exception e) {
            throw new UnoperationalStateException(e);
        }
    }

    /**
     * Build an instance of keycloak client authenticated to a realm.
     *
     * @param context Mandatory provider of connection settings.
     * @return A keycloak server stub over Keycloak Admin REST API.
     * @throws IllegalArgumentException When mandatory parameter is not defined or is invalid.
     */
    private Keycloak getKeycloakAdminClient(IContext context) throws IllegalArgumentException, UnoperationalStateException {
        if (context == null) throw new IllegalArgumentException("context parameter is required!");
        if (keycloakAdminClient != null) {
            // Check if previous connector is instantiated and is operational (e.g; Keycloak instance have not been undeployed or client decommissionned which shall be re-established
            if (!keycloakAdminClient.isClosed()) {
                return keycloakAdminClient;
            }
        }
        // Read dynamically name of Keycloak master realm (supporting any environment configuration change)
        masterRealmName = context.get(AdminConfigurationVariable.REALM_MASTER_NAME); // read current envt variable state

        // Create or re-instantiate singleton instance to Keycloak server over its administration API client
        keycloakAdminClient = KeycloakBuilder.builder()
                // TODO read config from context variables (e.g. admin account credentials, server url from environment variables)
                .serverUrl("http://localhost:8081")
                .realm(masterRealmName/* master realm */)
                .clientId("admin-cli")
                .grantType("password")
                .username("admin")
                .password("admin")
                .build();
        return keycloakAdminClient;
    }

    @Override
    public Tenant createTenant(String tenantLabel) throws IllegalArgumentException, OperationException {
        if (tenantLabel == null || tenantLabel.isEmpty())
            throw new IllegalArgumentException("Tenant label parameter is required!");
        // TODO Creation of tenant to code

        // Create Keycloak realm instance over keycloak-authz-client connector
        // https://www.keycloak.org/securing-apps/authz-client documentation
        throw new OperationException("to implement!");
    }

    @Override
    public boolean deleteTenant(String tenantLabel, boolean force) {
        // TODO deletion of tenant to code

        // Check tenantLabel defined and requiring treatment
        if (tenantLabel == null || tenantLabel.isEmpty())
            return false; // Return false because null or empty label is non conformity call

        // Search existing Keycloak realm with same name
        // When not found realm with same label, confirme deletion as effective (=current state of unexisting realm with same name)

        // When realm found, check if important dependent sub-data are existing (e.g; user accounts)
        // If none important sub-data found: delete the realm instance and confirm executed deletion

        // If forcing required: delete the realm including all any sub-informations
        // If forcing not required: don't execute deletion and confirm not executed for cause of existing important sub-data

        return false;
    }
}
