package org.cybnity.application.accesscontrol.ui.system.backend;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test of behaviors regarding the public API root url.
 *
 * @author olivier
 */
@ExtendWith({VertxExtension.class})
public class APIRootResourceUseCaseTest extends ContextualizedTest {

    private HttpClient client;

    @BeforeEach
    @DisplayName("Deploy backend verticle")
    void prepare(Vertx vertx, VertxTestContext testContext) {
        // Create instance of Http client
        int serverPort = Integer
                .parseInt(context.get(AppConfigurationVariable.REACTIVE_BACKEND_ENDPOINT_HTTP_SERVER_PORT));
        var options = new HttpClientOptions().setDefaultHost("localhost").setDefaultPort(serverPort);
        vertx.deployVerticle(new AccessControlBackendServer(), testContext.succeedingThenComplete());
        this.client = vertx.createHttpClient(options);
    }

    private Future<HttpClientRequest> prepareRequest(HttpMethod methodType, String resourceURLPath) {
        return client.request(methodType, apiRootURL + resourceURLPath);
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
        JsonObject command = JsonObject.of("RegisterOrganisation", JsonObject.of("organizationNaming", "CYBNITY"));
        logger.log(Level.INFO, command.toString());

        // Send command it to RestAPI service "/organizations/:organizationNaming"
        ConcreteCommandEvent
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
