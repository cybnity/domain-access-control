package org.cybnity.application.accesscontrol.ui.system.backend;

import org.junit.jupiter.api.TestInstance;

/**
 * Custom contextualized test containers that is enhanced of reusable features relative to repository preparation, and add requirements used by backend layer.
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class BackendCustomContextualizedTest extends CustomContextualizedTest {

    /**
     * Port of backend http server exposing reactive services for front end.
     */
    static Integer REACTIVE_ENDPOINT_HTTP_SERVER_PORT = 8082;

    /**
     * System address
     */
    static String REACTIVE_ENDPOINT_SERVER_HOST = "localhost";

    /**
     * Whitelisted servers' urls authorized for access to backend endpoint services.
     */
    static String WHITE_LIST_ORIGIN_SERVER_URLS = "http://" + REACTIVE_ENDPOINT_SERVER_HOST + ":" + REACTIVE_ENDPOINT_HTTP_SERVER_PORT + ",http://" + REACTIVE_ENDPOINT_SERVER_HOST + ":3000";

    /**
     * Base path of eventbus api.
     */
    static String REACTIVE_EVENTBUS_API_ROOT_PATH = "ac";

    public BackendCustomContextualizedTest(boolean withRedis, boolean withJanusGraph, boolean withKeycloak, boolean stopKeycloakAfterEach, boolean supportedBySnapshotRepository) {
        super(withRedis, withJanusGraph, withKeycloak, stopKeycloakAfterEach, supportedBySnapshotRepository);
    }

    @Override
    protected void initGatewayVariables() {
        super.initGatewayVariables();
        // Define additional environment variables regarding backend gateway
        environmentVariables.set(
                AppConfigurationVariable.REACTIVE_BACKEND_ENDPOINT_HTTP_SERVER_PORT.getName(),
                REACTIVE_ENDPOINT_HTTP_SERVER_PORT);
        environmentVariables.set(
                AppConfigurationVariable.AUTHORIZED_WHITE_LIST_ORIGIN_SERVER_URLS.getName(),
                WHITE_LIST_ORIGIN_SERVER_URLS);
        environmentVariables.set(AppConfigurationVariable.REACTIVE_EVENTBUS_DOMAIN_ROOT_PATH.getName(),
                REACTIVE_EVENTBUS_API_ROOT_PATH);
    }
}
