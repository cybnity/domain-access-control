package org.cybnity.application.accesscontrol.ui.system.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.cybnity.application.accesscontrol.ui.api.event.AttributeName;
import org.cybnity.application.accesscontrol.ui.api.event.CommandType;
import org.cybnity.application.accesscontrol.ui.api.experience.CollectionResourceArchetype;
import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.ObjectMapperBuilder;
import org.cybnity.framework.domain.event.ConcreteCommandEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.cybnity.framework.domain.event.CommandFactory;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Test of behaviors regarding the public API root url.
 *
 * @author olivier
 */
@ExtendWith({VertxExtension.class})
public class APIRootResourceUseCaseTest extends ContextualizedTest {

    private HttpClient client;
    private ObjectMapper mapper;
    private WebClient webClient;

    @BeforeEach
    @DisplayName("Deploy backend verticle")
    void prepare(Vertx vertx, VertxTestContext testContext) {
        // Create instance of Http client
        int serverPort = Integer
                .parseInt(context.get(AppConfigurationVariable.REACTIVE_BACKEND_ENDPOINT_HTTP_SERVER_PORT));
        var options = new HttpClientOptions().setDefaultHost("localhost").setDefaultPort(serverPort);
        vertx.deployVerticle(new AccessControlBackendServer(), testContext.succeedingThenComplete());
        this.client = vertx.createHttpClient(options);
        webClient = WebClient.wrap(this.client);
        mapper = new ObjectMapperBuilder().dateFormat().enableIndentation().preserveOrder(true).build();
    }

    private Future<HttpClientRequest> prepareRequest(HttpMethod methodType, String resourceURLPath) {
        String resource = apiRootURL + resourceURLPath;
        return client.request(methodType, resource);
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
    void givenNoExistingTenant_whenRegisterOrganization_thenOrganizationActioned(Vertx vertx, VertxTestContext testContext) throws Exception {
        // Prepare json object (RegisterOrganization command event including organization naming) from translator

        Collection<Attribute> definition = new ArrayList<>();
        // Set organization name
        Attribute tenantNameToRegister = new Attribute(AttributeName.OrganizationNaming.name(), "CYBNITY");
        definition.add(tenantNameToRegister);

        // Prepare RegisterOrganization command event to perform via API
        Command requestEvent = CommandFactory.create(CommandType.REGISTER_ORGANIZATION.name(),
                /* No identified as anonymous transaction without correlation id need*/ null,
                definition,
                /* none prior command to reference*/ null,
                /* None pre-identified organization because new creation */ null
        );
        // Auto-assign correlation identifier allowing finalized transaction check
        requestEvent.generateCorrelationId(null);

        JsonNode requestBody = mapper.readTree(mapper.writeValueAsString(requestEvent));

        // Send command it to RestAPI collection resource path
        webClient.post(apiRootURL + "/" + CollectionResourceArchetype.ORGANIZATIONS.label()).putHeader("Content-Type", "application/json").sendJson(requestBody, ar -> {
            if (ar.succeeded()) {
                HttpResponse<Buffer> response = ar.result();
                // Verify the confirmation of the treatment processing started by the server-side
                // with future return of async result
                Assertions.assertEquals(102, response.statusCode());

                // --- CASE : organizationActioned about tenantID of created or reassigned organization
                // Listen organizationActioned

            } else {
                Assertions.fail(ar.cause());
            }
            testContext.completeNow();
        });
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
        ConcreteCommandEvent commandEvent = new ConcreteCommandEvent();
        // TODO dans le constructeur, entrer une Enum de Type RegisterOrganization issue d'un catalogue de commandes supportées par le backend
        // depuis une dépendance venant de l'adapter du domaine ac-adapter-api

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
