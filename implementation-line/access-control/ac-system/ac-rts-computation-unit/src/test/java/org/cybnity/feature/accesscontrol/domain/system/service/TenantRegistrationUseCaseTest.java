package org.cybnity.feature.accesscontrol.domain.system.service;

import io.lettuce.core.StreamMessage;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.cybnity.application.accesscontrol.translator.ui.api.ACDomainMessageMapperFactory;
import org.cybnity.application.accesscontrol.translator.ui.api.UICapabilityChannel;
import org.cybnity.application.accesscontrol.ui.api.event.AttributeName;
import org.cybnity.application.accesscontrol.ui.api.event.CommandName;
import org.cybnity.application.accesscontrol.ui.api.event.TenantRegistrationAttributeName;
import org.cybnity.feature.accesscontrol.domain.system.CustomContextualizedTest;
import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.IdentifierStringBased;
import org.cybnity.framework.domain.event.CommandFactory;
import org.cybnity.framework.domain.model.DomainEntity;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.IMessageMapperProvider;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.Stream;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.UISAdapter;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.UISAdapterRedisImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Usage tests regarding the tenant registration application service exposed by the pipelined feature.
 * It is the use case scenarios test of "Register Tenant" commands treatment as specified by functional requirements.
 * This test scope is focused on integration validation between collaborative components (e.g event sourcing promotion) and error cases.
 */
@ExtendWith({VertxExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class TenantRegistrationUseCaseTest extends CustomContextualizedTest {

    /**
     * Identifiers of deployment
     */
    private String featureModuleId;

    private Thread featureModule;

    private UISAdapter uisClient;

    private Stream featureEndpointChannel;

    private IMessageMapperProvider mapperFactory;

    /**
     * Default constructor.
     */
    public TenantRegistrationUseCaseTest() {
        super(true, true, false, false, /* With snapshots management capability activated */ true);
    }

    @BeforeEach
    public void initThreads(Vertx vertx) throws Exception {
        // Define the supported message mapper relative to the Feature domain
        mapperFactory = new ACDomainMessageMapperFactory();

        // Prepare the definition of the feature pipeline entrypoint channel
        featureEndpointChannel = new Stream(UICapabilityChannel.access_control_tenant_registration.shortName());

        // Initialize an adapter connected to contextualized Redis server (Users Interactions Space)
        uisClient = new UISAdapterRedisImpl(context());

        // Prepare feature module executable instance
        featureModule = new Thread(() -> {
            // Start feature pipeline module
            vertx.deployVerticle(TenantRegistrationFeaturePipeline.class.getName()).onSuccess(id -> {
                featureModuleId = id;
                logger.fine("Tenant Registration feature pipeline is started");
            });
        });
    }

    /**
     * Test registration of new no existing organization.
     */
    @Test
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void givenNoExistingTenant_whenRegisterTenant_thenTenantActioned(Vertx vertx, VertxTestContext testContext) throws Exception {
        // USE CASE TEST SCENARIO: organizationActioned about tenantID newly defined in Identity server and tenant domain as usable for new user account creation
// TODO coding of use case test and acceptance rules (don't forget to activate start of keycloak required by registration process)
        // UTILITIES PREPARATION
        // Creation of parallel observer of SSO server admin event relative to new realm creation confirmations

        // START FEATURE MODULE
        featureModule.start();
        featureModule.join(); // wait end of module start execution

        // TEST PROCESSING
        // Request execution of a new RegisterOrganization command over the UIS
        String organizationName = "givenNoExistingTenant_whenRegisterTenant_thenTenantActioned";
        Command cmd = prepareRegisterTenantCommand(organizationName);
        String messageId = uisClient.append(cmd, featureEndpointChannel, mapperFactory.getMapper(cmd.getClass(), StreamMessage.class));
        // - search tenant control normally does not find any existing same organization named
        // - new Tenant is created for organization name by the feature
        // - Realm representation with configuration is automatically created and registered into the Customer Identity Management service (identity server)
        //   - access control settings are activated by the SSO server (e.g Keycloak)
        // - Realm resource dedicated to the organization name is defined and in active status

        // CHECKPOINT: Receive confirmation of registered new Tenant for organization description as actioned organization
        // CHECKPOINT: Verify the received tenantID created/assigned for organization
        // CHECKPOINT: Verify that SSO server admin event (saveRegisteredOrganization confirmation as realm for organization/tenant name) was received
        Checkpoint checkpoint = testContext.checkpoint(3); // Set the qty of check required to be flagged with success by this test

        checkpoint.flag();
        checkpoint.flag();
        checkpoint.flag();

        testContext.completeNow();
    }

    /**
     * Test of authorized re-assignment configuration of existing tenant not already activated, to new requester.
     */
    @Test
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void givenExistingTenantNotActivatedAndAuthorizedReassignment_whenRegisterTenant_thenTenantActioned(Vertx vertx, VertxTestContext testContext) throws Exception {
        // USE CASE TEST SCENARIO: organizationActioned about tenantID is reassigned as dedicated to new user account creation
// TODO coding of use case test and acceptance rules(don't forget to activate start of keycloak required by registration process)
        // UTILITIES PREPARATION
        // Creation of parallel observer of SSO server admin event relative to new realm creation confirmations
        // Configuration of the feature to authorize the dynamic Real re-assigning of existing tenant to new requester when is not used
        // Creation of parallel observer of Tenant domain add feature relative to connector configuration recording

        // START FEATURE MODULE
        featureModule.start();
        featureModule.join(); // wait end of module start execution

        // TEST PROCESSING
        // Request execution of a new RegisterOrganization command over the UIS
        String organizationName = "givenExistingTenantNotActivatedAndAuthorizedReassignment_whenRegisterTenant_thenTenantActioned";
        Command cmd = prepareRegisterTenantCommand(organizationName);
        // - search tenant control normally and find an existing same tenant equals named as existing (e.g like reserved by another person)
        // - none valid user account already previously registered and activated (email verified) is found regarding the existing tenant
        // - existing equals Tenant reference is confirmed with same name, but confirmed as not used (zero active user account)
        // - feature setting is authorized to dynamically authorize Real re-assigning and made it with Tenant added into the domain as known organization (reserved)
        // CHECKPOINT: Receive confirmation of newly registered and actioned organization event including its description
        // CHECKPOINT: Verify the received tenantID about known and re-assignable organization able to be subject of new future user account registration
        // CHECKPOINT: Verify that SSO server admin event (saveRegisteredOrganization confirmation as realm for organization/tenant name) was received
        Checkpoint checkpoint = testContext.checkpoint(3); // Set the qty of check required to be flagged with success by this test

        checkpoint.flag();
        checkpoint.flag();
        checkpoint.flag();

        testContext.completeNow();
    }

    /**
     * Test of unauthorized re-assignment configuration of existing tenant not already activated, to new requester.
     */
    @Test
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void givenExistingTenantNotActivatedAndNotAuthorizedReassignment_whenRegisterTenant_thenTenantReused(Vertx vertx, VertxTestContext testContext) throws Exception {
        // USE CASE TEST SCENARIO: organizationActioned about tenantID is not reassigned but is usable for new user account creation
// TODO coding of use case test and acceptance rules(don't forget to activate start of keycloak required by registration process)
        // UTILITIES PREPARATION
        // Configuration of the feature to NOT authorize the dynamic Real re-assigning of existing tenant to new requester when is not used

        // START FEATURE MODULE
        featureModule.start();
        featureModule.join(); // wait end of module start execution

        // TEST PROCESSING
        // Request execution of a new RegisterOrganization command over the UIS
        String organizationName = "givenExistingTenantNotActivatedAndNotAuthorizedReassignment_whenRegisterTenant_thenTenantReused";
        Command cmd = prepareRegisterTenantCommand(organizationName);
        // - search tenant control normally and find an existing same tenant equals named as existing (e.g like reserved by another person)
        // - none valid user account already previously registered and activated (email verified) is found regarding the existing tenant
        // - existing equals Tenant reference is confirmed with same name, but confirmed as not used (zero active user account)
        // - feature setting is NOT authorized to dynamically authorize Real re-assigning and made it
        // CHECKPOINT: Receive confirmation of know organization description event including its description
        // CHECKPOINT: Verify the received tenantID of known organization able to be subject of new future user account registration
        Checkpoint checkpoint = testContext.checkpoint(2); // Set the qty of check required to be flagged with success by this test

        checkpoint.flag();
        checkpoint.flag();

        testContext.completeNow();
    }

    /**
     * Test rejected registration of existing organization already assigned to another contact.
     */
    @Test
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void givenExistingTenantActivated_whenRegisterTenant_thenTenantRegistrationRejected(Vertx vertx, VertxTestContext testContext) throws Exception {
        // USE CASE TEST SCENARIO: rejected creation for cause of existing named tenant that is already used by previous register
// TODO coding of use case test and acceptance rules(don't forget to activate start of keycloak required by registration process)
        // START FEATURE MODULE
        featureModule.start();
        featureModule.join(); // wait end of module start execution

        // TEST PROCESSING
        // Request execution of a new RegisterOrganization command over the UIS
        String organizationName = "givenExistingTenantActivated_whenRegisterTenant_thenTenantRegistrationRejected";
        Command cmd = prepareRegisterTenantCommand(organizationName);

        // - search tenant control normally and find an existing same tenant equals named as existing (e.g like used by another person)
        // - valid user account already previously registered and activated (email verified) is found regarding the existing tenant
        // - existing equals Tenant reference is confirmed with same name and already used by other population (active user accounts)
        // CHECKPOINT: Receive organization registration rejected event relative to existing/assigned tenant with used registered accounts
        // CHECKPOINT: Verify the conformity of cause of organization creation rejected
        Checkpoint checkpoint = testContext.checkpoint(2); // Set the qty of check required to be flagged with success by this test

        checkpoint.flag();
        checkpoint.flag();

        testContext.completeNow();
    }

    /**
     * Prepare and return a valid command valid to be treated by the feature entrypoint.
     *
     * @param tenantName Mandatory name of an organization subject of registration.
     * @return A prepared command.
     * @throws IllegalArgumentException When any mandatory parameter is missing.
     */
    private Command prepareRegisterTenantCommand(String tenantName) throws IllegalArgumentException {
        if (tenantName == null || tenantName.isEmpty())
            throw new IllegalArgumentException("organizationName parameter is required!");
        // Prepare json object (RegisterTenant command event including tenant naming) from translator
        Collection<Attribute> definition = new ArrayList<>();
        // Set organization name
        Attribute tenantNameToRegister = new Attribute(TenantRegistrationAttributeName.TENANT_NAMING.name(), tenantName);
        definition.add(tenantNameToRegister);
        // Define default activity state
        Attribute activityStatus = new Attribute(AttributeName.ACTIVITY_STATE.name(), Boolean.TRUE.toString());
        definition.add(activityStatus);
        // Prepare RegisterOrganization command event to perform via API
        Command cmd = CommandFactory.create(CommandName.REGISTER_TENANT.name(),
                new DomainEntity(IdentifierStringBased.generate(null)) /* command identity */, definition,
                /* none prior command to reference*/ null,
                /* None pre-identified organization because new creation */ null);
        // Auto-assign correlation identifier allowing finalized transaction check
        cmd.generateCorrelationId(UUID.randomUUID().toString());
        return cmd;
    }

}
