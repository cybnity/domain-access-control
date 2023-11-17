package org.cybnity.application.accesscontrol.ui.system.backend;

import org.cybnity.framework.Context;
import org.cybnity.framework.IContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.logging.Logger;

/**
 * Generic helped about unit test contextualized with environment variables.
 */
@ExtendWith(SystemStubsExtension.class)
public class ContextualizedTest {

    /**
     * Utility logger
     */
    protected Logger logger;

    /**
     * Environment variable defined as an operational executed container.
     */
    @SystemStub
    protected EnvironmentVariables environmentVariables;
    /**
     * Current context of adapter runtime.
     */
    protected IContext context;

    protected String apiRootURL;
    protected Integer httpServerPort;
    protected String whiteListOriginServerURLs;
    protected String serverHost;
    protected String apiRootPath;
    protected int workerInstances;
    protected int workerThreadPool;

    @BeforeEach
    public void initRedisConnectionChainValues() {
        logger = Logger.getLogger(this.getClass().getName());
        apiRootURL = "/api/access-control";
        apiRootPath = "ac";
        httpServerPort = 8080;
        serverHost = "localhost";
        whiteListOriginServerURLs = "http://localhost:8080,http://localhost:3000";
        workerInstances = 3;
        workerThreadPool = 3;

        // Build reusable context
        this.context = new Context();
        context.addResource(apiRootURL, "ENDPOINT_HTTP_RESOURCE_API_ROOT_URL", true);
        context.addResource(apiRootPath, "AppConfigurationVariable.REACTIVE_EVENTBUS_DOMAIN_ROOT_PATH", true);
        context.addResource(httpServerPort, "REACTIVE_BACKEND_ENDPOINT_HTTP_SERVER_PORT", true);
        context.addResource(whiteListOriginServerURLs, "AUTHORIZED_WHITE_LIST_ORIGIN_SERVER_URLS", true);
        context.addResource(workerInstances, "DOMAIN_WORKER_INSTANCES", true);
        context.addResource(workerThreadPool, "DOMAIN_WORKER_THREAD_POOL_SIZE", true);
        // Synchronize environment variables test values
        initEnvVariables();
    }

    /**
     * Define runtime environment variable set.
     */
    private void initEnvVariables() {
        // Define environment variables
        environmentVariables.set(
                AppConfigurationVariable.REACTIVE_BACKEND_ENDPOINT_HTTP_SERVER_PORT.getName(),
                httpServerPort);
        environmentVariables.set(
                AppConfigurationVariable.AUTHORIZED_WHITE_LIST_ORIGIN_SERVER_URLS.getName(),
                whiteListOriginServerURLs);
        environmentVariables.set(AppConfigurationVariable.REACTIVE_EVENTBUS_DOMAIN_ROOT_PATH.getName(),
                apiRootPath);
        environmentVariables.set(AppConfigurationVariable.DOMAIN_WORKER_THREAD_POOL_SIZE.getName(), workerThreadPool);
        environmentVariables.set(AppConfigurationVariable.DOMAIN_WORKER_INSTANCES.getName(), workerInstances);
    }

    /**
     * Clean all defined variables.
     */
    protected void removeAllEnvVariables() {
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
        removeAllEnvVariables();
        context = null;
        logger = null;
    }
}
