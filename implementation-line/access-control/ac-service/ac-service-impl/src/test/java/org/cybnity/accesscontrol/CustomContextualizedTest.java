package org.cybnity.accesscontrol;

import org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.CIAMWriteModelConfigurationVariable;
import org.cybnity.accesscontrol.domain.infrastructure.impl.ACWriteModelConfigurationVariable;
import org.cybnity.accesscontrol.domain.infrastructure.impl.TenantsStore;
import org.cybnity.accesscontrol.iam.domain.infrastructure.impl.IAMWriteModelConfigurationVariable;
import org.cybnity.application.accesscontrol.translator.ui.api.AccessControlDomainModel;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.model.IDomainModel;
import org.cybnity.infastructure.technical.persistence.store.impl.redis.PersistentObjectNamingConvention;
import org.cybnity.tool.test.InfrastructureContextualizedTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

/**
 * Custom contextualized test containers that is enhanced of reusable features relative to repository preparation.
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class CustomContextualizedTest extends InfrastructureContextualizedTest {

    private PersistentObjectNamingConvention.NamingConventionApplicability persistentObjectNamingConvention;

    private IDomainModel dataOwner;

    /**
     * Default duration in seconds of each IAM object snapshot version stored in Redis.
     */
    static protected Long IAM_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS = 60L;

    /**
     * Default duration in seconds of each CIAM object snapshot version stored in Redis.
     */
    static protected Long CIAM_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS = 60L;

    /**
     * Default Keycloak MASTER real name.
     */
    static protected String KEYCLOAK_REALM_MASTER_NAME = "cybnnity";

    /**
     * Default duration in seconds of each Access Control object snapshot version stored in Redis.
     */
    static protected Long AC_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS = 20L;

    public CustomContextualizedTest(boolean withRedis, boolean withJanusGraph, boolean withKeycloak, boolean stopKeycloakAfterEach, boolean supportedBySnapshotRepository) {
        super(withRedis, withJanusGraph, withKeycloak, stopKeycloakAfterEach, supportedBySnapshotRepository);
        // Initialize environment variable required by server adapter clients
        initServerClientsEnvironmentVariables();
    }

    @BeforeEach
    public void initEnhancedServiceData() throws Exception {
        // Initialize dedicated data and configurations
        persistentObjectNamingConvention = PersistentObjectNamingConvention.NamingConventionApplicability.TENANT;
        dataOwner = new AccessControlDomainModel();
    }

    /**
     * Initialize environment variable required by server adapter clients
     */
    protected void initServerClientsEnvironmentVariables() {
        initKeycloakEnvVariables(context());
        initRedisEnvVariables();
    }

    /**
     * Add complementary environment variables required by a unit test using an Access Control write model.
     */
    protected void initRedisEnvVariables() {
        super.initRedisEnvVariables(); // Define common variables
        if (environmentVariables != null) {
            // Define additional environment variables regarding domain write model
            environmentVariables.set(CIAMWriteModelConfigurationVariable.CIAM_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS.getName(), CIAM_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS);
            environmentVariables.set(IAMWriteModelConfigurationVariable.IAM_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS.getName(), IAM_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS);
            environmentVariables.set(ACWriteModelConfigurationVariable.AC_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS.getName(), AC_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS);
        }
    }

    /**
     * Add complementary environment variables required by a unit test using a Keycloak instance.
     *
     * @param context Context to update.
     */
    protected void initKeycloakEnvVariables(IContext context) {
        super.initKeycloakEnvVariables();
        if (environmentVariables != null) {
            // Define environment variables regarding adapter initialization
            environmentVariables.set(org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin.AdminConfigurationVariable.REALM_MASTER_NAME.getName(), KEYCLOAK_REALM_MASTER_NAME);
        }
        if (context != null) {
            // Set configuration resources required by Keycloak adapter
            context.addResource(KEYCLOAK_REALM_MASTER_NAME, org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin.AdminConfigurationVariable.REALM_MASTER_NAME.getName(), false);
        }
    }

    /**
     * Clean complementary data initialized before each test execution.
     */
    @AfterEach
    public void cleanAdditionalServiceData() {
        dataOwner = null;
        persistentObjectNamingConvention = null;
    }

    /**
     * Get a persistence store implementation with or without support of snapshots capabilities (defined by parameter during by the initial call of this class's constructor).
     *
     * @return A store singleton instance.
     * @throws UnoperationalStateException When impossible instantiation of the Redis store adapter.
     */
    protected TenantsStore getTenantPersistenceOrientedStore() throws UnoperationalStateException {
        return (TenantsStore) TenantsStore.instance(context(), dataOwner, persistentObjectNamingConvention, /* with or without help by a snapshots capability provider */ getSnaphotRepository());
    }

}
