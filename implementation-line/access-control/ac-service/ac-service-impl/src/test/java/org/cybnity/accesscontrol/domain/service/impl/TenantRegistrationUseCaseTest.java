package org.cybnity.accesscontrol.domain.service.impl;

import io.vertx.junit5.VertxExtension;
import org.cybnity.accesscontrol.CustomContextualizedTest;
import org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.mock.TenantMockHelper;
import org.cybnity.accesscontrol.domain.infrastructure.impl.TenantTransactionCollectionsRepository;
import org.cybnity.accesscontrol.domain.infrastructure.impl.TenantsStore;
import org.cybnity.accesscontrol.domain.infrastructure.impl.TenantsWriteModelImpl;
import org.cybnity.application.accesscontrol.adapter.api.admin.ISSOAdminAdapter;
import org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin.SSOAdminAdapterKeycloakImpl;
import org.cybnity.application.accesscontrol.translator.ui.api.ACDomainMessageMapperFactory;
import org.cybnity.application.accesscontrol.translator.ui.api.UICapabilityChannel;
import org.cybnity.application.accesscontrol.translator.ui.api.event.DomainEventType;
import org.cybnity.application.accesscontrol.ui.api.event.AttributeName;
import org.cybnity.application.accesscontrol.ui.api.event.TenantRegistrationAttributeName;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.*;
import org.cybnity.framework.domain.event.EventSpecification;
import org.cybnity.framework.domain.model.SessionContext;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.BaseConstants;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.Channel;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.ChannelObserver;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.IMessageMapperProvider;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.UISAdapter;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.UISAdapterRedisImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Behavior unit test regarding the registration cases. This test scope is not considering the integration concerns with repositories or event sourcing collaboration actions (based on mocked services).
 */
@ExtendWith({VertxExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class TenantRegistrationUseCaseTest extends CustomContextualizedTest {

    private TenantsStore tenantsStore;
    private TenantRegistration tenantRegistrationService;
    private ISessionContext sessionCtx;
    private TenantTransactionCollectionsRepository tenantsRepository;
    private String serviceName;
    private Channel featureTenantsChangesNotificationChannel;
    private UISAdapter client;
    private IMessageMapperProvider mapperFactory;

    /**
     * Default constructor.
     */
    public TenantRegistrationUseCaseTest() {
        super(true, true, true,false,true);
    }

    @BeforeEach
    public void initHelpers() throws UnoperationalStateException {
        // Create a store managing streamed messages
        tenantsStore = getTenantPersistenceOrientedStore();

        this.tenantsRepository = TenantTransactionCollectionsRepository.instance(context(), tenantsStore);
        this.sessionCtx = new SessionContext(null);
        this.serviceName = "TenantRegistrationService";
        this.featureTenantsChangesNotificationChannel = new Channel(UICapabilityChannel.access_control_tenants_changes.shortName());
        this.client = new UISAdapterRedisImpl(this.sessionCtx);
        ISSOAdminAdapter ssoClient = new SSOAdminAdapterKeycloakImpl(this.sessionCtx);
        this.mapperFactory = new ACDomainMessageMapperFactory();
        this.tenantRegistrationService = new TenantRegistration(sessionCtx, TenantsWriteModelImpl.instance(tenantsStore), tenantsRepository, serviceName, featureTenantsChangesNotificationChannel, this.client, ssoClient);

        // Check started keycloak instance and accessible admin api
        Assertions.assertNotNull(this.getKeycloak(), "shall have been started as defined in constructor super() call!");
    }

    @AfterEach
    public void clean() {
        this.tenantsRepository.freeUpResources();
        if (tenantsStore != null) tenantsStore.freeUpResources();
        tenantsStore = null;
        this.tenantsRepository = null;
        this.serviceName = null;
        this.sessionCtx = null;
        this.tenantRegistrationService = null;
        this.featureTenantsChangesNotificationChannel = null;
        this.client = null;
        this.mapperFactory = null;
    }

    /**
     * Unit test about CASE: create a new Tenant.
     */
    @Test
    public void givenUnexistingLabel_whenTenantRegistrationRequested_thenTenantCreated() throws Exception {
        // Prepare observer of registration service outputs
        Collection<ChannelObserver> outputObservers = new ArrayList<>();
        final String organizationName = "givenUnexistingLabel_whenTenantRegistrationRequested_thenTenantCreated";
        final CountDownLatch acceptancesCriteriaCheckResultsWaiter = new CountDownLatch(1 /* Qty of confirmed observer finalized treatments */);

        outputObservers.add(new ChannelObserver() {
            @Override
            public Channel observed() {
                return featureTenantsChangesNotificationChannel;
            }

            @Override
            public String observationPattern() {
                return null;
            }

            /**
             * Observer of success registration result which check that first added tenant was notified in channel.
             * @param evt Notification event about new added tenant
             */
            @Override
            public void notify(Object evt) {
                if (IDescribed.class.isAssignableFrom(evt.getClass())) {
                    IDescribed domainEvent = (IDescribed) evt;
                    // Read result of registration and verify that tenant have been created with success (notification event as new tenant created)
                    String eventType = domainEvent.type().value();

                    if (DomainEventType.TENANT_REGISTERED.name().equals(eventType)) {
                        // Detect only when a new tenant added notification is received
                        Collection<Attribute> spec = domainEvent.specification();
                        // Verify that added tenant have same organization (organizationName) name that tested command
                        Attribute nameAttr = EventSpecification.findSpecificationByName(TenantRegistrationAttributeName.TENANT_NAMING.name(), spec);
                        Assertions.assertNotNull(nameAttr);
                        Assertions.assertEquals(organizationName, nameAttr.value()); // Valid created tenant for organization name
                        Attribute isActive = EventSpecification.findSpecificationByName(AttributeName.ACTIVITY_STATE.name(), spec);
                        Assertions.assertEquals(Boolean.FALSE, Boolean.valueOf(isActive.value())); // default defined value is false
                        Attribute serviceName = EventSpecification.findSpecificationByName(org.cybnity.framework.domain.event.AttributeName.SERVICE_NAME.name(), spec);
                        Assertions.assertNotNull(serviceName.value()); // Service name defined
                        Attribute id = EventSpecification.findSpecificationByName(AttributeName.TENANT_ID.name(), spec);
                        Assertions.assertNotNull(id.value());// Defined tenant identifier

                        // Verify if stored tenant can be retrieved from store
                        Tenant retrieved = null;
                        try {
                            retrieved = tenantsStore.findEventFrom(new IdentifierStringBased(BaseConstants.IDENTIFIER_ID.name(), id.value()));
                        } catch (UnoperationalStateException e) {
                            throw new RuntimeException(e);
                        }
                        Assertions.assertNotNull(retrieved);

                        acceptancesCriteriaCheckResultsWaiter.countDown();
                    }
                }
            }
        });
        this.client.subscribe(outputObservers, mapperFactory.getMapper(String.class, IDescribed.class));

        // Prepare a new registration command
        Command cmd = TenantMockHelper.prepareRegisterTenantCommand(organizationName, Boolean.FALSE);

        // Submit to registration service
        this.tenantRegistrationService.handle(cmd); // Tenant shall have been added into the events store

        // Wait for give time to message to be processed
        Assertions.assertTrue(acceptancesCriteriaCheckResultsWaiter.await(80, TimeUnit.SECONDS), "Timeout reached before collaboration messages treated!");
        // Remove observers from the channels provider
        this.client.unsubscribe(outputObservers);
    }

    /**
     * Unit test about CASE: tenant (e.g platform tenant with same name) retrieved is re-assignable AND REGISTERED SHALL BE NOTIFIED.
     */
    @Test
    public void givenExistingLabelAndNotActive_whenTenantRegistrationRequested_thenOldRegistrationRetrieved() throws Exception {
        final String organizationName = "givenExistingLabelAndNotActive_whenTenantRegistrationRequested_thenOldRegistrationRetrieved";
        final CountDownLatch acceptancesCriteriaCheckResultsWaiter = new CountDownLatch(2 /* Qty of confirmed observer finalized treatments */);
        // Prepare observer of registration service outputs
        Collection<ChannelObserver> outputObservers = new ArrayList<>();

        // Prepare and execute a tenant registration (simulating a first creation already performed by a start of company's platform instance)
        Command cmd = TenantMockHelper.prepareRegisterTenantCommand(organizationName, Boolean.FALSE);
        // Simulate that it is an operational status that is not already defined as activated for registered users

        outputObservers.add(new ChannelObserver() {
            @Override
            public Channel observed() {
                return featureTenantsChangesNotificationChannel;
            }

            @Override
            public String observationPattern() {
                return null;
            }

            /**
             * Observer of success registration result which check that first added tenant was notified in channel.
             * @param evt Notification event about new added tenant
             */
            @Override
            public void notify(Object evt) {
                if (IDescribed.class.isAssignableFrom(evt.getClass())) {
                    IDescribed domainEvent = (IDescribed) evt;
                    // Read result of registration and verify that tenant have been created with success (notification event as new tenant created)
                    String eventType = domainEvent.type().value();

                    if (DomainEventType.TENANT_REGISTERED.name().equals(eventType)) {
                        // Detect only when a new tenant added notification is received
                        // or an already existing tenant with same organization name have been retrieved without new creation

                        Collection<Attribute> spec = domainEvent.specification();
                        // Verify that added tenant have same organization (organizationName) name that tested command
                        Attribute nameAttr = EventSpecification.findSpecificationByName(TenantRegistrationAttributeName.TENANT_NAMING.name(), spec);
                        Assertions.assertNotNull(nameAttr);

                        acceptancesCriteriaCheckResultsWaiter.countDown();
                    }
                }
            }
        });
        this.client.subscribe(outputObservers, mapperFactory.getMapper(String.class, IDescribed.class));

        // Submit to registration service
        this.tenantRegistrationService.handle(cmd); // Tenant shall have been added into the events store

        // Prepare and try to execute tenant registration with same name (simulating company's platform instance re-start)
        Command cmd2 = TenantMockHelper.prepareRegisterTenantCommand(organizationName, Boolean.FALSE);

        // Attempt to create a new tenant with the same name that shall be not added but shall be retrieved because not already active
        this.tenantRegistrationService.handle(cmd2); // Tenant shall have not been added into the events store

        // Wait for give time to message to be processed
        Assertions.assertTrue(acceptancesCriteriaCheckResultsWaiter.await(80, TimeUnit.SECONDS), "Timeout reached before collaboration messages treated!");
        // Remove observers from the channels provider
        this.client.unsubscribe(outputObservers);
    }

}
