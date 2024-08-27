package org.cybnity.test.util;

import org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.CIAMWriteModelConfigurationVariable;
import org.cybnity.accesscontrol.domain.infrastructure.impl.ACWriteModelConfigurationVariable;
import org.cybnity.accesscontrol.domain.infrastructure.impl.TenantsStore;
import org.cybnity.accesscontrol.iam.domain.infrastructure.impl.IAMWriteModelConfigurationVariable;
import org.cybnity.application.accesscontrol.ui.api.AccessControlDomainModel;
import org.cybnity.framework.Context;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.application.vertx.common.AppConfigurationVariable;
import org.cybnity.framework.domain.ISessionContext;
import org.cybnity.framework.domain.infrastructure.ISnapshotRepository;
import org.cybnity.framework.domain.model.IDomainModel;
import org.cybnity.framework.domain.model.SessionContext;
import org.cybnity.infastructure.technical.persistence.store.impl.redis.PersistentObjectNamingConvention;
import org.cybnity.infastructure.technical.persistence.store.impl.redis.SnapshotRepositoryRedisImpl;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.ReadModelConfigurationVariable;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.WriteModelConfigurationVariable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;
import redis.embedded.RedisServer;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.logging.Logger;

/**
 * Generic helped about unit test contextualized with environment variables.
 * <p>
 * Automatic configuration and start of Redis, JanusGraph, Keycloak servers usable during a test execution according to unit test instantiation configuration.
 * Each unit test requiring optionally a Redis and/or JanusGraph and/or Keycloak server started, shall extend this class.
 */
@ExtendWith({SystemStubsExtension.class})
public class ContextualizedTest {

    private PersistentObjectNamingConvention.NamingConventionApplicability persistentObjectNamingConvention;

    private IDomainModel dataOwner;

    private ISnapshotRepository snapshotsRepo;

    /**
     * Utility logger
     */
    protected Logger logger;

    /**
     * Test context.
     */
    private IContext context;

    /**
     * Current started process' environment variables.
     */
    @SystemStub
    protected EnvironmentVariables environmentVariables;

    static protected Integer HTTP_SERVER_PORT = 8080;
    static protected int WORKER_INSTANCES = 1;
    static protected int WORKER_THREAD_POOL = 1;

    /**
     * Redis instance optionally started.
     */
    protected RedisServer redisServer;

    /**
     * Keycloak instance optionally started.
     */
    private GenericContainer<?> keycloak;

    /**
     * Redis server auth password.
     * Read Redis Kubernetes configuration's REDISCLI_AUTH environment variable
     */
    static protected String DEFAULT_AUTH_PASSWORD = "1gEGHneiLT";
    /**
     * System address
     */
    static protected String SERVER_HOST = "localhost";
    /**
     * Default port
     */
    static protected int SERVER_PORT = 6379;
    /**
     * Default user account declared on Redis server.
     */
    static protected String CONNECTION_USER_ACCOUNT = "default";
    /**
     * Default first db number
     */
    static protected String DATABASE_NUMBER = "1";
    /**
     * Default duration in seconds of each IAM object snapshot version stored in Redis.
     */
    static protected Long IAM_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS = 60L;

    /**
     * Default duration in seconds of each CIAM object snapshot version stored in Redis.
     */
    static protected Long CIAM_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS = 60L;

    /**
     * Default duration in seconds of each Access Control object snapshot version stored in Redis.
     */
    static protected Long AC_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS = 20L;

    /**
     * In-memory storage backend.
     */
    static public final String STORAGE_BACKEND_TYPE = "inmemory";

    protected ISessionContext sessionCtx;

    private final boolean activeRedis;
    private final boolean activeJanusGraph;
    private final boolean activeKeycloak;

    /**
     * True if Keycloak existing instance shall be stopped after each test execution.
     */
    private final boolean stopKeycloakAfterEach;

    /**
     * Default constructor.
     *
     * @param withRedis             True when Redis embedded server shall be started.
     * @param withJanusGraph        True when JanusGraph embedded server shall be started.
     * @param withKeycloak          True when Keycloak embedded server shall be started.
     * @param stopKeycloakAfterEach True if Keycloak server instance shall be stopped after each test execution. False when none stop shall be performed.
     */
    public ContextualizedTest(boolean withRedis, boolean withJanusGraph, boolean withKeycloak, boolean stopKeycloakAfterEach) {
        // Define each desired services
        this.activeRedis = withRedis;
        this.activeJanusGraph = withJanusGraph;
        this.activeKeycloak = withKeycloak;
        this.stopKeycloakAfterEach = stopKeycloakAfterEach;
    }

    /**
     * Default constructor with Keycloak instance (when desired by parameter) not stopped after each unit test execution.
     *
     * @param withRedis      True when Redis embedded server shall be started.
     * @param withJanusGraph True when JanusGraph embedded server shall be started.
     * @param withKeycloak   True when Keycloak embedded server shall be started.
     */
    public ContextualizedTest(boolean withRedis, boolean withJanusGraph, boolean withKeycloak) {
        this(withRedis, withJanusGraph, withKeycloak, /* reuse same Keycloak instance by default without stop after each unit test execution */ false);
    }

    @BeforeEach
    public void initServices() throws Exception {
        try {
            // Initialize shared data and configurations
            logger = Logger.getLogger(this.getClass().getName());
            persistentObjectNamingConvention = PersistentObjectNamingConvention.NamingConventionApplicability.TENANT;
            dataOwner = new AccessControlDomainModel();
            // Build reusable context
            this.context = new Context();
            this.sessionCtx = new SessionContext(null);

            if (this.activeJanusGraph)
                // Set JanusGraph repository environment
                setJanusGraphServer(this.context);

            // Synchronize all environment variables test values
            initEnvVariables();

            if (this.activeRedis)
                // Set Redis server environment
                setRedisServer();

            if (this.activeKeycloak) // Set Keycloak server environment
                setKeycloakServer();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Define runtime environment variable set.
     */
    protected void initEnvVariables() {
        if (this.activeJanusGraph)
            initJanusGraphEnvVariables();
        if (this.activeRedis)
            initRedisEnvVariables();
        if (this.activeKeycloak)
            initKeycloakEnvVariables();
        initGatewayVariables();
    }

    private void setKeycloakServer() {
        // Get server image ready for start (singleton instance)
        boolean reusableActivation = !stopKeycloakAfterEach; // Inverse of reuse requested by this class constructor
        keycloak = SSOTestContainer.getKeycloakContainer(reusableActivation);
        // Start the Keycloak server if not already running
        SSOTestContainer.start(keycloak);
        // Check finalized initial start
        Assertions.assertTrue(keycloak.isRunning(), "Shall have been started via command environment variable!");
    }

    private void setRedisServer() {
        // Start Redis instance (EmbeddedRedisExtension.class for Redis 6.0.5 used by default)
        // See https://redis.io/docs/management/config-file/ for more detail about supported start options
        redisServer = RedisServer.builder().port(SERVER_PORT)
                .setting("bind 127.0.0.1 -::1") // good for local development on Windows to prevent security popups
                //.slaveOf(SERVER_HOST, 6378)
                .setting("daemonize no")
                .setting("masteruser " + CONNECTION_USER_ACCOUNT)
                .setting("masterauth " + DEFAULT_AUTH_PASSWORD)
                //.setting("databases " + DATABASE_NUMBER)
                //.setting("appendonly no")
                //.setting("maxmemory 128M")
                .build();
        // Start redis server usable by worker
        redisServer.start();
    }

    private void setJanusGraphServer(IContext context) {
        // Set configuration resources required by JanusGraph server
        context.addResource(STORAGE_BACKEND_TYPE, org.cybnity.infrastructure.technical.registry.adapter.impl.janusgraph.ReadModelConfigurationVariable.JANUSGRAPH_STORAGE_BACKEND.getName(), false);
    }

    private void initKeycloakEnvVariables() {
        if (environmentVariables != null) {
            // Define environment variables regarding server initialization
        }
    }

    private void initJanusGraphEnvVariables() {
        if (environmentVariables != null) {
            // Define environment variables regarding server initialization
            environmentVariables.set(
                    org.cybnity.infrastructure.technical.registry.adapter.impl.janusgraph.ReadModelConfigurationVariable.JANUSGRAPH_STORAGE_BACKEND.getName(),
                    STORAGE_BACKEND_TYPE);
        }
    }

    private void initRedisEnvVariables() {
        // Define environment variables regarding write model
        environmentVariables.set(
                WriteModelConfigurationVariable.REDIS_WRITEMODEL_CONNECTION_DEFAULT_AUTH_PASSWORD.getName(),
                DEFAULT_AUTH_PASSWORD);
        environmentVariables.set(
                WriteModelConfigurationVariable.REDIS_WRITEMODEL_CONNECTION_DEFAULT_USERACCOUNT.getName(),
                CONNECTION_USER_ACCOUNT);
        environmentVariables.set(WriteModelConfigurationVariable.REDIS_WRITEMODEL_DATABASE_NUMBER.getName(),
                DATABASE_NUMBER);
        environmentVariables.set(WriteModelConfigurationVariable.REDIS_WRITEMODEL_SERVER_HOST.getName(), SERVER_HOST);
        environmentVariables.set(WriteModelConfigurationVariable.REDIS_WRITEMODEL_SERVER_PORT.getName(), Integer.toString(SERVER_PORT));
        environmentVariables.set(CIAMWriteModelConfigurationVariable.CIAM_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS.getName(), CIAM_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS);
        environmentVariables.set(IAMWriteModelConfigurationVariable.IAM_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS.getName(), IAM_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS);
        environmentVariables.set(ACWriteModelConfigurationVariable.AC_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS.getName(), AC_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS);

        // Variables regarding read model
        environmentVariables.set(
                ReadModelConfigurationVariable.REDIS_READMODEL_CONNECTION_DEFAULT_AUTH_PASSWORD.getName(),
                DEFAULT_AUTH_PASSWORD);
        environmentVariables.set(
                ReadModelConfigurationVariable.REDIS_READMODEL_CONNECTION_DEFAULT_USERACCOUNT.getName(),
                CONNECTION_USER_ACCOUNT);
        environmentVariables.set(ReadModelConfigurationVariable.REDIS_READMODEL_DATABASE_NUMBER.getName(),
                DATABASE_NUMBER);
        environmentVariables.set(ReadModelConfigurationVariable.REDIS_READMODEL_SERVER_HOST.getName(), SERVER_HOST);
        environmentVariables.set(ReadModelConfigurationVariable.REDIS_READMODEL_SERVER_PORT.getName(), Integer.toString(SERVER_PORT));
    }

    private void initGatewayVariables() {
        // Define additional environment variables regarding gateway
        environmentVariables.set(
                AppConfigurationVariable.ENDPOINT_HTTP_SERVER_PORT.getName(),
                HTTP_SERVER_PORT);
        environmentVariables.set(AppConfigurationVariable.DOMAIN_WORKER_THREAD_POOL_SIZE.getName(), WORKER_THREAD_POOL);
        environmentVariables.set(AppConfigurationVariable.DOMAIN_WORKER_INSTANCES.getName(), WORKER_INSTANCES);

        // Define workers environment variables
    }

    /**
     * Be care full that when a Keycloak instance have been started, it is not automatically stopped after each unit test execution.
     * A reusable Keycloak instance previously started (according to this class's constructor defined parameter's value), shall be manually stopped by any subclass of this contextualized test.
     */
    @AfterEach
    public void cleanValues() {
        if (snapshotsRepo != null) snapshotsRepo.freeResources();
        snapshotsRepo = null;
        if (this.activeRedis)
            // Stop redis server used by worker
            redisServer.stop();
        if (this.stopKeycloakAfterEach) {
            if (getKeycloak() != null)
                // Stop keycloak server because shall not be reused by next test
                SSOTestContainer.stop(getKeycloak());
        }
        this.environmentVariables = null;
        this.redisServer = null;
        this.keycloak = null;
        context = null;
        logger = null;
        dataOwner = null;
        persistentObjectNamingConvention = null;
    }


    /**
     * Get test context.
     *
     * @return A context instance including environment variable names and values.
     */
    protected IContext getContext() {
        return this.context;
    }


    /**
     * Get a persistence store implementation with or without support of snapshots capabilities.
     *
     * @param supportedBySnapshotRepository True when snapshots usage shall be configured into the returned store.
     * @return A store.
     * @throws UnoperationalStateException When impossible instantiation of the Redis adapter.
     */
    protected TenantsStore getPersistenceOrientedStore(boolean supportedBySnapshotRepository) throws UnoperationalStateException {
        snapshotsRepo = (supportedBySnapshotRepository) ? new SnapshotRepositoryRedisImpl(getContext()) : null;
        // Voluntary don't use instance() method to avoid singleton capability usage during this test campaign
        return (TenantsStore) TenantsStore.instance(getContext(), dataOwner, persistentObjectNamingConvention, /* with or without help by a snapshots capability provider */ snapshotsRepo);
    }

    /**
     * Get started Keycloak instance.
     *
     * @return Instance or null (when none started according to the test's constructor execution defined).
     */
    protected GenericContainer<?> getKeycloak() {
        return this.keycloak;
    }
}
