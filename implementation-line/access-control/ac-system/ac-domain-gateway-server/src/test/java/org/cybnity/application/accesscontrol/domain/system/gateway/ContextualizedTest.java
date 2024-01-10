package org.cybnity.application.accesscontrol.domain.system.gateway;

import org.cybnity.framework.Context;
import org.cybnity.framework.IContext;
import org.cybnity.framework.application.vertx.common.AppConfigurationVariable;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.ReadModelConfigurationVariable;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.WriteModelConfigurationVariable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import redis.embedded.RedisServer;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.logging.Logger;

/**
 * Generic helped about unit test contextualized with environment variables.
 * <p>
 * Auto-configuration and start of Redis container usable during a test execution.
 * Each unit test requiring a redis container started shall extend this class.
 * EmbeddedRedisExtension.class for Redis 6.0.5 used by default
 */
@ExtendWith({SystemStubsExtension.class})
public class ContextualizedTest {

    /**
     * Utility logger
     */
    protected Logger logger;

    /**
     * Test context.
     */
    protected IContext context;

    /**
     * Current started process' environment variables.
     */
    @SystemStub
    protected EnvironmentVariables environmentVariables;

    static protected Integer HTTP_SERVER_PORT = 8080;
    static protected int WORKER_INSTANCES = 3;
    static protected int WORKER_THREAD_POOL = 3;

    /**
     * Start and stop of the server shall be manually managed by child test class.
     */
    protected RedisServer redisServer;

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

    @BeforeEach
    public void initRedisConnectionChainValues() {
        logger = Logger.getLogger(this.getClass().getName());

        // Build reusable context
        this.context = new Context();

        // Synchronize environment variables test values
        initEnvVariables();

        // Start Redis instance
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

    /**
     * Define runtime environment variable set.
     */
    protected void initEnvVariables() {
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

        // Define additional environment variables regarding gateway
        environmentVariables.set(
                AppConfigurationVariable.ENDPOINT_HTTP_SERVER_PORT.getName(),
                HTTP_SERVER_PORT);
        environmentVariables.set(AppConfigurationVariable.DOMAIN_WORKER_THREAD_POOL_SIZE.getName(), WORKER_THREAD_POOL);
        environmentVariables.set(AppConfigurationVariable.DOMAIN_WORKER_INSTANCES.getName(), WORKER_INSTANCES);
    }


    @AfterEach
    public void cleanValues() {
        // Stop redis server used by worker
        redisServer.stop();
        this.environmentVariables = null;
        this.redisServer = null;
        context = null;
        logger = null;
    }
}
