package org.cybnity.application.accesscontrol.ui.system.backend;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.cybnity.framework.Context;
import org.cybnity.framework.IContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Test of behaviors regarding the public API root url.
 *
 * @author olivier
 */
@ExtendWith({VertxExtension.class, SystemStubsExtension.class})
public class APIRootResourceUseCaseTest {

    @SystemStub
    private static EnvironmentVariables environmentVariables;

    /**
     * Current context of adapter runtime.
     */
    private final IContext context = new Context();
    private HttpClient client;
    private final static Logger LOGGER = Logger.getLogger(APIRootResourceUseCaseTest.class.getName());
    private String rootUrl;

    @BeforeEach
    @DisplayName("Deploy backend verticle")
    void prepare(Vertx vertx, VertxTestContext testContext) {
        int serverPort = Integer
                .parseInt(context.get(AppConfigurationVariable.REACTIVE_BACKEND_ENDPOINT_HTTP_SERVER_PORT));
        var options = new HttpClientOptions().setDefaultHost("localhost").setDefaultPort(serverPort);
        vertx.deployVerticle(new AccessControlBackendServer(), testContext.succeedingThenComplete());

        this.client = vertx.createHttpClient(options);
        this.rootUrl = context.get(AppConfigurationVariable.ENDPOINT_HTTP_RESOURCE_API_ROOT_URL);
    }

    @BeforeAll
    public static void showVariableEnvironmentToSet() {
        Map<String, String> envVariables = environmentVariables.getVariables();
        envVariables.forEach((k, v) -> System.out.println((k + ":" + v)));

        String b = "---------\nCheck that environment variables are defined into the test configuration:\n" +
                "ENDPOINT_HTTP_RESOURCE_API_ROOT_URL=/api/access-control\n" +
                "REACTIVE_BACKEND_ENDPOINT_HTTP_SERVER_PORT=8080\n" +
                "AUTHORIZED_WHITE_LIST_ORIGIN_SERVER_URLS=http://localhost:8080,http://localhost:3000" +
                "\n---------";
        System.out.println(b);
    }

    private Future<HttpClientRequest> prepareRequest(HttpMethod methodType, String resourceURLPath) {
        return client.request(methodType, rootUrl + resourceURLPath);
    }

    /**
     * Test that root url of domain backend does not provide resource and only return an error status code.
     */
    @Test
    @DisplayName("Check root path refused as 404 error code")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void givenRootPath_whenCalled_then404HTTPError(Vertx vertx, VertxTestContext testContext) {
        // See Vert.x default code about unknown route at https://vertx.io/docs/3.9.16/vertx-web/java/#_route_match_failures

        // Call domain root url
        prepareRequest(HttpMethod.GET,
                "")
                .flatMap(HttpClientRequest::send)
                .onComplete(testContext.succeeding(
                        response -> testContext.verify(
                                () -> {
                                    assertEquals(404, response.statusCode());
                                    testContext.completeNow();
                                }
                        )
                ));
    }

    /**
     * Test registration of new no existing organization with callback of organization actioned
     */
    @Test
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void givenNoExistingTenant_whenRegisterOrganization_thenOrganizationActioned(Vertx vertx, VertxTestContext testContext) {
        // Prepare json object (RegisterOrganization command event including organization naming)

        // Send command it to RestAPI service "/organizations/:organizationNaming"

        // --- CASE : organizationActioned about tenantID of created or reassigned organization
        // Listen organizationActioned

        testContext.completeNow();
    }

    /**
     * Test rejected registration of existing organization already assigned to another contact.
     */
    @Test
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void givenExistingTenant_whenRegisterOrganization_thenOrganizationActioned(Vertx vertx, VertxTestContext testContext) {
        // Prepare json object (RegisterOrganization command event including organization naming)

        // Send command it to RestAPI service "/organizations/:organizationNaming"

        // --- CASE : rejected creation for cause of existing named organization that is already used by previous register
        // Listen potential existing tenant [existingTenant != null && existingTenant.validUsers() > 0]

        testContext.completeNow();
    }

    /**
     * Test registration of existing organization that was not assigned, with callback of organization reassigned
     */
    @Test
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void givenNotAssignedExistingTenant_whenRegisterOrganization_thenOrganizationReassigned(Vertx vertx, VertxTestContext testContext) {
        // Prepare json object (RegisterOrganization command event including organization naming)

        // Send command it to RestAPI service "/organizations/:organizationNaming"

        // --- CASE : organizationActioned about tenantID of created or reassigned organization
        // Listen organizationActioned because [(existingTenant != null && existingTenant.validUsers() == 0) as re-assignable to new requestor]
        testContext.completeNow();
    }
}
