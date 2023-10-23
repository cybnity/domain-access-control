package org.cybnity.application.accesscontrol.ui.system.backend;

import org.cybnity.framework.Context;
import org.cybnity.framework.IContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

/**
 * Generic helped about unit test contextualized with environment variables.
 */
@ExtendWith(SystemStubsExtension.class)
public class ContextualizedTest {

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

    @BeforeEach
    public void initRedisConnectionChainValues() {
        apiRootURL = "/api/access-control";
        httpServerPort = 8080;
        whiteListOriginServerURLs = "http://localhost:8080,http://localhost:3000";

        // Build reusable context
        this.context = new Context();
        context.addResource(apiRootURL, "ENDPOINT_HTTP_RESOURCE_API_ROOT_URL", true);
        context.addResource(httpServerPort, "REACTIVE_BACKEND_ENDPOINT_HTTP_SERVER_PORT", true);
        context.addResource(whiteListOriginServerURLs, "AUTHORIZED_WHITE_LIST_ORIGIN_SERVER_URLS", true);
        // Synchronize environment variables test values
        initEnvVariables();
    }

    /**
     * Define runtime environment variable set.
     */
    private void initEnvVariables() {
        // Define environment variables
        environmentVariables.set(
                AppConfigurationVariable.ENDPOINT_HTTP_RESOURCE_API_ROOT_URL.getName(),
                apiRootURL);
        environmentVariables.set(
                AppConfigurationVariable.REACTIVE_BACKEND_ENDPOINT_HTTP_SERVER_PORT.getName(),
                httpServerPort);
        environmentVariables.set(
                AppConfigurationVariable.AUTHORIZED_WHITE_LIST_ORIGIN_SERVER_URLS.getName(),
                whiteListOriginServerURLs);
    }

    /**
     * Clean all defined variables.
     */
    protected void removeAllEnvVariables() {
        environmentVariables.set(
                AppConfigurationVariable.ENDPOINT_HTTP_RESOURCE_API_ROOT_URL.getName(),
                null);
        environmentVariables.set(
                AppConfigurationVariable.REACTIVE_BACKEND_ENDPOINT_HTTP_SERVER_PORT.getName(),
                null);
        environmentVariables.set(
                AppConfigurationVariable.AUTHORIZED_WHITE_LIST_ORIGIN_SERVER_URLS.getName(),
                null);
    }

    @AfterEach
    public void cleanValues() {
        removeAllEnvVariables();
        context = null;
    }
}
