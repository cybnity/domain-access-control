package org.cybnity.application.accesscontrol.ui.system.backend;

import org.cybnity.framework.Context;
import org.cybnity.framework.IContext;
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
 * Auto configuration and start of Redis container usable during a test execution.
 * Each unit test requiring a redis container started shall extend this class.
 * EmbeddedRedisExtension.class for Redis 6.0.5 used by default
 */
@ExtendWith({SystemStubsExtension.class})
public class ContextualizedTest {

    /**
     * Utility logger
     */
    protected Logger logger;

    protected IContext context;

    /**
     * Current started process' environment variables.
     */
    @SystemStub
    protected EnvironmentVariables environmentVariables;

    static String API_ROOT_URL = "/api/access-control";
    static Integer HTTP_SERVER_PORT = 8080;
    static String WHITE_LIST_ORIGIN_SERVER_URLS = "http://localhost:8080,http://localhost:3000";
    static String API_ROOT_PATH = "ac";
    static int WORKER_INSTANCES = 3;
    static int WORKER_THREAD_POOL = 3;

    private RedisServer redisServer;

    /**
     * Redis server auth password.
     * Read Redis Kubernetes configuration's REDISCLI_AUTH environment variable
     */
    static String DEFAULT_AUTH_PASSWORD = "1gEGHneiLT";
    /**
     * System address
     */
    static String SERVER_HOST = "localhost";
    /**
     * Default port
     */
    static int SERVER_PORT = 6379;
    /**
     * Default user account declared on Redis server.
     */
    static String CONNECTION_USER_ACCOUNT = "default";
    /**
     * Default first db number
     */
    static String DATABASE_NUMBER = "1";

    @BeforeEach
    public void initRedisConnectionChainValues() {
        logger = Logger.getLogger(this.getClass().getName());

        // Build reusable context
        this.context = new Context();

        context.addResource(DEFAULT_AUTH_PASSWORD, "defaultAuthPassword", false);
        context.addResource(SERVER_HOST, "serverHost", false);
        context.addResource(Integer.toString(SERVER_PORT), "serverPort", false);
        context.addResource(DATABASE_NUMBER, "databaseNumber", false);
        context.addResource(CONNECTION_USER_ACCOUNT, "connectionUserAccount", false);
        context.addResource(DEFAULT_AUTH_PASSWORD, "connectionPassword", false);

        context.addResource(API_ROOT_URL, "ENDPOINT_HTTP_RESOURCE_API_ROOT_URL", true);
        context.addResource(API_ROOT_PATH, "AppConfigurationVariable.REACTIVE_EVENTBUS_DOMAIN_ROOT_PATH", true);
        context.addResource(HTTP_SERVER_PORT, "REACTIVE_BACKEND_ENDPOINT_HTTP_SERVER_PORT", true);
        context.addResource(WHITE_LIST_ORIGIN_SERVER_URLS, "AUTHORIZED_WHITE_LIST_ORIGIN_SERVER_URLS", true);
        context.addResource(WORKER_INSTANCES, "DOMAIN_WORKER_INSTANCES", true);
        context.addResource(WORKER_THREAD_POOL, "DOMAIN_WORKER_THREAD_POOL_SIZE", true);
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

        // Define additional environment variables regarding backend gateway
        environmentVariables.set(
                AppConfigurationVariable.REACTIVE_BACKEND_ENDPOINT_HTTP_SERVER_PORT.getName(),
                HTTP_SERVER_PORT);
        environmentVariables.set(
                AppConfigurationVariable.AUTHORIZED_WHITE_LIST_ORIGIN_SERVER_URLS.getName(),
                WHITE_LIST_ORIGIN_SERVER_URLS);
        environmentVariables.set(AppConfigurationVariable.REACTIVE_EVENTBUS_DOMAIN_ROOT_PATH.getName(),
                API_ROOT_PATH);
        environmentVariables.set(AppConfigurationVariable.DOMAIN_WORKER_THREAD_POOL_SIZE.getName(), WORKER_THREAD_POOL);
        environmentVariables.set(AppConfigurationVariable.DOMAIN_WORKER_INSTANCES.getName(), WORKER_INSTANCES);
    }

    /**
     * Clean all defined variables.
     */
    protected void removeAllEnvVariables() {
        // Variables regarding write model
        environmentVariables.set(
                WriteModelConfigurationVariable.REDIS_WRITEMODEL_CONNECTION_DEFAULT_AUTH_PASSWORD.getName(),
                null);
        environmentVariables.set(
                WriteModelConfigurationVariable.REDIS_WRITEMODEL_CONNECTION_DEFAULT_USERACCOUNT.getName(),
                null);
        environmentVariables.set(WriteModelConfigurationVariable.REDIS_WRITEMODEL_DATABASE_NUMBER.getName(),
                null);
        environmentVariables.set(WriteModelConfigurationVariable.REDIS_WRITEMODEL_SERVER_HOST.getName(), null);
        environmentVariables.set(WriteModelConfigurationVariable.REDIS_WRITEMODEL_SERVER_PORT.getName(), null);
        // Variables regarding read model
        environmentVariables.set(
                ReadModelConfigurationVariable.REDIS_READMODEL_CONNECTION_DEFAULT_AUTH_PASSWORD.getName(),
                null);
        environmentVariables.set(
                ReadModelConfigurationVariable.REDIS_READMODEL_CONNECTION_DEFAULT_USERACCOUNT.getName(),
                null);
        environmentVariables.set(ReadModelConfigurationVariable.REDIS_READMODEL_DATABASE_NUMBER.getName(),
                null);
        environmentVariables.set(ReadModelConfigurationVariable.REDIS_READMODEL_SERVER_HOST.getName(), null);
        environmentVariables.set(ReadModelConfigurationVariable.REDIS_READMODEL_SERVER_PORT.getName(), null);
        // Variables regarding backend gateway
        environmentVariables.set(
                AppConfigurationVariable.REACTIVE_BACKEND_ENDPOINT_HTTP_SERVER_PORT.getName(),
                null);
        environmentVariables.set(
                AppConfigurationVariable.AUTHORIZED_WHITE_LIST_ORIGIN_SERVER_URLS.getName(),
                null);
        environmentVariables.set(AppConfigurationVariable.REACTIVE_EVENTBUS_DOMAIN_ROOT_PATH.getName(),
                null);
        environmentVariables.set(
                AppConfigurationVariable.DOMAIN_WORKER_THREAD_POOL_SIZE.getName(),
                null);
        environmentVariables.set(
                AppConfigurationVariable.DOMAIN_WORKER_INSTANCES.getName(),
                null);
    }

    @AfterEach
    public void cleanValues() {
        redisServer.stop();
        removeAllEnvVariables();
        context = null;
        logger = null;
    }
}
