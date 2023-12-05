package org.cybnity.application.accesscontrol.ui.system.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.cybnity.application.accesscontrol.ui.api.event.AttributeName;
import org.cybnity.application.accesscontrol.ui.api.event.CommandName;
import org.cybnity.application.accesscontrol.ui.api.event.DomainEventType;
import org.cybnity.application.accesscontrol.ui.system.backend.routing.CollaborationChannel;
import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.DomainEvent;
import org.cybnity.framework.domain.ObjectMapperBuilder;
import org.cybnity.framework.domain.event.CommandFactory;
import org.cybnity.framework.domain.event.ConcreteCommandEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Test of behaviors regarding the public registration service of new tenant.
 *
 * @author olivier
 */
@ExtendWith({VertxExtension.class})
public class PublicOrganizationRegistrationUseCaseTest extends ContextualizedTest {

    private HttpClient client;
    private ObjectMapper mapper;
    private Vertx vertx;

    @BeforeEach
    @DisplayName("Prepare gateway server verticle")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void prepareGatewayServer(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        // Create instance of Http client allowing communication over SockJS server
        var options = new HttpClientOptions().setDefaultHost(SERVER_HOST).setDefaultPort(HTTP_SERVER_PORT);
        vertx.deployVerticle(DomainBackendMessagingGateway.class.getName(), testContext.succeeding(id -> {
            this.client = vertx.createHttpClient(options);
            mapper = new ObjectMapperBuilder().dateFormat().enableIndentation().preserveOrder(true).build();
            logger.fine("Access control messaging gateway server prepared");
            testContext.completeNow();
        }));
    }

    @AfterEach
    @DisplayName("Free test resources")
    void freeResources() throws Exception {
        vertx.close();
        mapper = null;
        this.vertx = null;
    }

    /**
     * Test registration of new no existing organization with callback of organization actioned
     */
    @Test
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void givenNoExistingTenant_whenRegisterOrganization_thenOrganizationActioned(Vertx vertx, VertxTestContext testContext) throws Exception {
        EventBus eb = vertx.eventBus();
        // Prepare json object (RegisterOrganization command event including organization naming) from translator
        Collection<Attribute> definition = new ArrayList<>();
        // Set organization name
        Attribute tenantNameToRegister = new Attribute(AttributeName.OrganizationNaming.name(), "CYBNITY");
        definition.add(tenantNameToRegister);
        // Prepare RegisterOrganization command event to perform via API
        Command requestEvent = CommandFactory.create(CommandName.REGISTER_ORGANIZATION.name(),
                /* No identified as anonymous transaction without correlation id need*/ null, definition,
                /* none prior command to reference*/ null,
                /* None pre-identified organization because new creation */ null);
        // Auto-assign correlation identifier allowing finalized transaction check
        requestEvent.generateCorrelationId(null);

        // Send command request over event bus
        DeliveryOptions options = new DeliveryOptions();
        /* X-Request-ID, X-Correlation-ID or Correlation-ID common non-standard request fields */
        options.addHeader("Correlation-ID", requestEvent.correlationId().value());
        options.addHeader("Content-Type", "application/json");
        String requestCmd = mapper.writeValueAsString(requestEvent);
        // Transform command event into vertx supported JsonObject type allowing binding
        JsonObject message = new JsonObject(requestCmd);

        // Start redis server usable by worker
        redisServer.start();

        // Send command it to access control public worker
        eb.request(CollaborationChannel.ac_in_public_organization_registration.label(), message, options, reply -> {
            if (reply.succeeded()) {
                try {
                    List<Map.Entry<String, String>> headers = reply.result().headers().entries();

                    // Check if correlation id transport is ensured from request to response flow
                    boolean foundCorrelationId = false;
                    for (Map.Entry<String, String> header : headers) {
                        if ("Correlation-ID".equalsIgnoreCase(header.getKey())) {
                            foundCorrelationId = requestEvent.correlationId().value().equals(header.getValue());
                        }
                    }
                    Assertions.assertTrue(foundCorrelationId, "Response shall returned result with defined correlation id!");

                    // Check received activated registration reservation
                    Object body = reply.result().body();
                    Assertions.assertNotNull(body, "Response shall include registration JSON object!");

                    // Test read of JsonNode version
                    JsonNode changeEventNode = mapper.readTree(body.toString());
                    Assertions.assertEquals("DomainEvent", changeEventNode.get("@class").asText(), "Invalid type of domain event returned by service!");

                    // Test read of POJO version
                    DomainEvent changedEvent = mapper.readValue(body.toString(), DomainEvent.class);
                    String globalTransactionIdRetrieved = changedEvent.correlationId().value();
                    Assertions.assertEquals(requestEvent.correlationId().value(), globalTransactionIdRetrieved, "Shall have been maintained during all the registration process executed!");

                    // Check existing returned good organization actioned type equals to DomainEventType.ORGANIZATION_REGISTERED.name()
                    boolean isRegisteredOrganization = false, isGoodNamedTenant = false;
                    for (Attribute spec : changedEvent.specification()) {
                        // Is it an event about registered organization?
                        if ("type".equals(spec.name()) && DomainEventType.ORGANIZATION_REGISTERED.name().equals(spec.value())) {
                            isRegisteredOrganization = true;
                        }
                        // Is organization naming attribute is valued?
                        if (tenantNameToRegister.name().equals(spec.name()) && tenantNameToRegister.value().equals(spec.value())) {
                            isGoodNamedTenant = true;
                        }
                    }
                    Assertions.assertTrue(isRegisteredOrganization, "The organization shall have been registered!");
                    Assertions.assertTrue(isGoodNamedTenant, "The created tenant shall have been correctly named!");

                    testContext.completeNow();
                } catch (Exception me) {
                    testContext.failNow(me);
                } finally {
                    // Stop redis server used by worker
                    redisServer.stop();
                }
            } else {
                redisServer.stop();
                testContext.failNow(reply.cause());
            }
        });
    }

    /**
     * Test rejected registration of existing organization already assigned to another contact.
     */
    @Test
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void givenExistingTenant_whenRegisterOrganization_thenOrganizationActioned(Vertx vertx, VertxTestContext testContext) {
        // Prepare json object (RegisterOrganization command event including organization naming)
        JsonObject command = JsonObject.of(CommandName.REGISTER_ORGANIZATION.name(), JsonObject.of("organizationNaming", "CYBNITY"));
        //logger.log(Level.INFO, command.toString());

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
     *
     @Test
     @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
     void givenNotAssignedExistingTenant_whenRegisterOrganization_thenOrganizationReassigned(Vertx vertx, VertxTestContext testContext) {
     // Prepare json object (RegisterOrganization command event including organization naming)

     // Send command it to RestAPI service "/organizations/:organizationNaming"

     // --- CASE : organizationActioned about tenantID of created or reassigned organization
     // Listen organizationActioned because [(existingTenant != null && existingTenant.validUsers() == 0) as re-assignable to new requestor]
     testContext.completeNow();
     }
     */
}
