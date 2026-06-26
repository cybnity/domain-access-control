package org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin;

import org.cybnity.accesscontrol.domain.model.TenantDTO;
import org.cybnity.application.accesscontrol.adapter.api.admin.IAccessAdminAdapter;
import org.cybnity.application.accesscontrol.adapter.api.admin.OperationException;
import org.cybnity.application.accesscontrol.translator.keycloak.api.mapper.KeycloakMapperFactory;
import org.cybnity.application.accesscontrol.translator.keycloak.api.mapper.RealmMapper;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.keycloak.domain.model.Realm;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ServerInfoResource;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.AccessTokenResponse;

import java.time.OffsetDateTime;
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
     * Limit date of expiration of the latest token provided by Keycloak server.
     */
    private OffsetDateTime currentTokenExpiringAt;

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

        // Check the minimum required data allowing connection to the targeted Keycloak server
        checkHealthyState();

        // Test connection to Keycloak instance over the Admin REST API client library
        // See https://www.keycloak.org/securing-apps/admin-client documentation about Keycloak admin client usage
        try {
            ServerInfoResource res = getKeycloakAdminClient(context).serverInfo();
            if (res == null || res.getInfo() == null)
                throw new UnoperationalStateException(" Server info resource not found in Keycloak server!");
        } catch (Exception e) {
            throw new UnoperationalStateException(e);
        }
    }

    @Override
    public void freeUpResources() {
        // Disconnect Keycloak REST API stub is existing
        if (keycloakAdminClient != null) {
            keycloakAdminClient.close();
        }
    }

    @Override
    public void checkHealthyState() throws UnoperationalStateException {
        if (healthyChecker == null)
            healthyChecker = new ExecutableAdminAdapterChecker(context);
        // Execution the health check about configuration
        healthyChecker.checkOperableState();
        // Connection test is only performed by constructor to allow reuse of this configuration check by the getKeycloakAdminClient method without overread of test calls to Keycloak server each time when admin configuration variable are changed during runtime
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
            // Check if previous connector is instantiated and is operational (e.g; Keycloak instance have not been undeployed or client decommissioned which shall be re-established
            if (!keycloakAdminClient.isClosed()) {
                manageTokenUpdate(keycloakAdminClient); // Manage eventual access token expiration requiring its refresh for new period
                return keycloakAdminClient;
            }
        }

        // --- READ TARGETED SERVER CONFIGURATION FROM ENVIRONMENT VARIABLES DEFINED INTO THE SYSTEM USING THIS ADAPTER INSTANCE ---
        healthyChecker.checkOperableState(); // Execution the health check about configuration potentially upgraded (e.g; with undefined values error!)

        // Create or re-instantiate singleton instance to Keycloak server over its administration API client
        // See https://www.keycloak.org/securing-apps/admin-client documentation

        // See admin client library source code project at https://github.com/keycloak/keycloak-client/blob/main/admin-client/src/main/java/org/keycloak/admin/client/Keycloak.java
        // the builder implementation uses a default RestEasy client builder settings
        keycloakAdminClient = KeycloakBuilder.builder()
                // Read current up-to-date administration client configuration allowing Keycloak Admin Client API usage of master realm (supporting any environment configuration HOT changes)
                .serverUrl(context.get(AdminConfigurationVariable.KEYCLOAK_SERVER_URL))
                .realm(context.get(AdminConfigurationVariable.REALM_MASTER_NAME /* Keycloak default master realm as currently configured into Keycloak server */))
                .clientId(context.get(AdminConfigurationVariable.REALM_MASTER_CLIENTID))
                .grantType(context.get(AdminConfigurationVariable.REALM_MASTER_GRANT_TYPE))
                .username(context.get(AdminConfigurationVariable.REALM_MASTER_USERNAME))
                .password(context.get(AdminConfigurationVariable.REALM_MASTER_PASSWORD))
                .build();

        // Initialize an original access token for this client, which is usable during a period (defined in Keycloak server)
        manageTokenUpdate(keycloakAdminClient);

        return keycloakAdminClient; // return operational client ready for use
    }

    /**
     * Define the current token expiring date based from access token provided by Keycloak Admin Client API library.
     * This method is synchronized on the remote call to Keycloak. When the expiration date of the current token is not reached, the Keycloak Admin Client only return the current period before next expiration.
     * But if Keycloak Admin Client lib identify that a expiration date is reached, it makes a call to Keycloak server for received a new refresh token available for a new period.
     * The currentTokenExpiringAt attribute is upgraded automatically for the new date of future expiration as limit for the usable token.
     *
     * @param adminClient Mandatory client.
     * @throws IllegalArgumentException    When mandatory parameter is missing.
     * @throws UnoperationalStateException When any exception occurred by Keycloak Admin Client library, or during call to Keycloak server.
     */
    private void manageTokenUpdate(Keycloak adminClient) throws IllegalArgumentException, UnoperationalStateException {
        if (adminClient == null) throw new IllegalArgumentException("adminClient parameter is required!");
        try {
            boolean requiredExpirationLimitRefresh = false;

            // Check if latest provided token is not expired and should be refreshed before to return the client instance
            // Evaluate if potential token expiration of previous client instance is expired (and need to be refreshed before to return operational client)
            // or is not granted (e.g; changed account user name or password in Keycloak server-side web console requiring to instantiate new client based on new authentication account from environment variable normally also upgraded)
            if (currentTokenExpiringAt != null) {
                // Verify if expiration date is reached justifying call to received refresh token
                // Optimization rule: Potential duration of this SYNCHRONIZED CALL TO KEYCLOAK SERVER BY ADMIN CLIENT!
                if (!/* past fact */ currentTokenExpiringAt.isAfter(OffsetDateTime.now()) /* no more time for token usage */) {
                    requiredExpirationLimitRefresh = true;
                }
                // Else none new call requested because the client can continue to use the current access token currently in the expiration period not reached
            } else {
                // None initial date have been allowed by Keycloak (e.g; none access token have been originally received by Keycloak Admin client lib
                requiredExpirationLimitRefresh = true;
            }
            if (requiredExpirationLimitRefresh) {
                // Identify initial date of expiration based on current access token
                // Request current access token from token manager, that will be automatically managed (automatically refreshed by TokenManager when expire date reached)
                TokenManager tokenMgt = adminClient.tokenManager();
                AccessTokenResponse accessToken = tokenMgt.getAccessToken(); // Potential duration of this synchronized call! If previous original access token expired, a refresh is automatically executed by the token manager for allow continuity of the client
                long tokenExpiredInSeconds = accessToken.getExpiresIn(); // Duration in seconds before end of validity
                // Identify date when refresh of token will be required
                currentTokenExpiringAt = defineExpirationLimitDate(OffsetDateTime.now() /* from now */, tokenExpiredInSeconds /* max duration in seconds */);
            }
        } catch (Exception e) {
            throw new UnoperationalStateException(e);
        }
    }

    /**
     * Calculate and return an expiration date based on a date including a limitation duration in seconds.
     *
     * @param since          Mandatory origin date.
     * @param afterInSeconds Duration in seconds. When null, since date parameter is returned.
     * @return Calculated date of expiration.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    private OffsetDateTime defineExpirationLimitDate(OffsetDateTime since, Long afterInSeconds) throws IllegalArgumentException {
        if (since == null) throw new IllegalArgumentException("since parameter is required!");
        if (afterInSeconds != null) {
            // Add duration in seconds (or reduce duration from origin data if negative
            return since.plusSeconds(afterInSeconds);
        }
        return since;
    }

    @Override
    public TenantDTO createTenant(String tenantLabel) throws IllegalArgumentException, OperationException {
        if (tenantLabel == null || tenantLabel.isEmpty())
            throw new IllegalArgumentException("Tenant label parameter is required!");
        // Create Keycloak realm instance over keycloak-authz-client connector
        // https://www.keycloak.org/securing-apps/authz-client documentation

        // TODO Creation of tenant to code into Keycloak over its Admin client
        // TODO create Realm object into Keycloak and get instance state including configuration elements (e.g; potential technical settings allowing its technical identification or usage options to synchronize into Tenant object for CYBNITY domain)
        Realm.Status currentState = Realm.Status.REALM_DISABLED; // TEMP DATA VALUE
        String retrievedName = tenantLabel;// TEMP TEST VALUE
        Realm instance = new Realm(retrievedName, currentState);

        // Transform created Realm instance (Keycloak ontology based) into CYBNITY Access Control DTO (including eventual configuration elements state)
        RealmMapper mapper = new KeycloakMapperFactory().createRealmMapper();
        return mapper.toDTO(instance);
    }

    @Override
    public boolean deleteTenant(String tenantLabel, boolean force) {
        // TODO deletion of tenant to code

        // Check tenantLabel defined and requiring treatment
        if (tenantLabel == null || tenantLabel.isEmpty())
            return false; // Return false because null or empty label is non conformity call

        // Search existing Keycloak realm with same name into Keycloak over its API client
        // When not found realm with same label, confirm deletion as effective (=current state of unexisting realm with same name)

        // When realm found, check if important dependent sub-data are existing (e.g; user accounts)
        // If none important sub-data found: delete the realm instance and confirm executed deletion

        // If forcing required: delete the realm including all any sub-information
        // If forcing not required: don't execute deletion and confirm not executed for cause of existing important sub-data

        return false;
    }
}
