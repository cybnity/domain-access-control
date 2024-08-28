package org.cybnity.accesscontrol.domain.service.impl;

import io.vertx.junit5.VertxExtension;
import org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.mock.TenantMockHelper;
import org.cybnity.accesscontrol.domain.infrastructure.impl.TenantTransactionCollectionsRepository;
import org.cybnity.accesscontrol.domain.infrastructure.impl.TenantsStore;
import org.cybnity.accesscontrol.domain.infrastructure.impl.TenantsWriteModelImpl;
import org.cybnity.accesscontrol.domain.service.api.ApplicationServiceOutputCause;
import org.cybnity.application.accesscontrol.adapter.api.SSOAdapter;
import org.cybnity.application.accesscontrol.adapter.impl.keycloak.SSOAdapterKeycloakImpl;
import org.cybnity.application.accesscontrol.translator.ui.api.ACDomainMessageMapperFactory;
import org.cybnity.application.accesscontrol.ui.api.UICapabilityChannel;
import org.cybnity.application.accesscontrol.ui.api.event.AttributeName;
import org.cybnity.application.accesscontrol.ui.api.event.TenantRegistrationAttributeName;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.IDescribed;
import org.cybnity.framework.domain.ISessionContext;
import org.cybnity.framework.domain.event.EventSpecification;
import org.cybnity.framework.domain.model.SessionContext;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.Channel;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.ChannelObserver;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.IMessageMapperProvider;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.UISAdapter;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.UISAdapterRedisImpl;
import org.cybnity.test.util.ContextualizedTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Behavior unit test regarding the registration rejection cases. This test scope is not considering the integration concerns with repositories or event sourcing collaboration actions (based on mocked services).
 */
@ExtendWith({VertxExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class TenantRegistrationRejectionUseCaseTest extends ContextualizedTest {

    private TenantsStore tenantsStore;
    private TenantRegistration tenantRegistrationService;
    private ISessionContext sessionCtx;
    private TenantTransactionCollectionsRepository tenantsRepository;
    private String serviceName;
    private Channel featureTenantsChangesNotificationChannel;
    private UISAdapter uisClient;
    private IMessageMapperProvider mapperFactory;

    /**
     * Default constructor.
     */
    public TenantRegistrationRejectionUseCaseTest() {
        super(true, true, /* not need by registration service use case impl */ false);
    }

    @BeforeEach
    public void initHelpers() throws UnoperationalStateException {
        // Create a store managing streamed messages
        tenantsStore = getPersistenceOrientedStore(true /* With snapshots management capability activated */);

        this.tenantsRepository = TenantTransactionCollectionsRepository.instance(getContext(), tenantsStore);
        this.sessionCtx = new SessionContext(null);
        this.serviceName = "TenantRegistrationService";
        this.featureTenantsChangesNotificationChannel = new Channel(UICapabilityChannel.access_control_tenants_changes.shortName());
        this.uisClient = new UISAdapterRedisImpl(this.sessionCtx);
        SSOAdapter ssoClient = new SSOAdapterKeycloakImpl(this.sessionCtx);
        this.mapperFactory = new ACDomainMessageMapperFactory();
        this.tenantRegistrationService = new TenantRegistration(sessionCtx, TenantsWriteModelImpl.instance(tenantsStore), tenantsRepository, serviceName, featureTenantsChangesNotificationChannel, this.uisClient, ssoClient);
    }

    @AfterEach
    public void clean() {
        this.tenantsRepository.freeResources();
        if (tenantsStore != null) tenantsStore.freeResources();
        tenantsStore = null;
        this.tenantsRepository = null;
        this.serviceName = null;
        this.sessionCtx = null;
        this.tenantRegistrationService = null;
        this.featureTenantsChangesNotificationChannel = null;
        this.uisClient = null;
        this.mapperFactory = null;
    }

    /**
     * Unit test about CASE: tenant (e.g platform tenant with same name, and already active for existing user account) creation is rejected AND REJECTION SHALL BE NOTIFIED.
     */
    @Test
    public void givenExistingAndActiveLabel_whenTenantRegistrationRequested_thenRegistrationRejected() throws Exception {
        final String organizationName = "givenExistingAndActiveLabel_whenTenantRegistrationRequested_thenRegistrationRejected";
        final CountDownLatch acceptancesCriteriaCheckResultsWaiter = new CountDownLatch(1 /* Qty of confirmed observer finalized treatments */);

        // Prepare observer of registration service outputs
        Collection<ChannelObserver> outputObservers = new ArrayList<>();
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
             * Observer of rejected registration result with event verification.
             * @param evt Notification event about new added tenant
             */
            @Override
            public void notify(Object evt) {
                if (IDescribed.class.isAssignableFrom(evt.getClass())) {
                    IDescribed domainEvent = (IDescribed) evt;
                    // Read result and verify that tenant registration have been rejected (notification event as new tenant request rejection)
                    String eventType = domainEvent.type().value();
                    if (org.cybnity.application.accesscontrol.ui.api.event.DomainEventType.TENANT_REGISTRATION_REJECTED.name().equals(eventType)) {
                        // CASE: tenant (e.g platform tenant with same name and already in an operational activity status not re-assignable) creation is not authorized AND REJECTION SHALL BE NOTIFIED
                        Collection<Attribute> spec = domainEvent.specification();
                        // Verify that rejected tenant creation is about same organization (organizationName) name that tested command
                        Attribute nameAttr = EventSpecification.findSpecificationByName(TenantRegistrationAttributeName.TENANT_NAMING.name(), spec);
                        Assertions.assertNotNull(nameAttr);
                        Assertions.assertEquals(organizationName, nameAttr.value()); // Valid retrieved tenant label about organization name

                        // Verify that retrieved Tenant is not able to be re-assigned because already in operational status
                        Attribute isActive = EventSpecification.findSpecificationByName(AttributeName.ACTIVITY_STATE.name(), spec);
                        Assertions.assertEquals(Boolean.TRUE, Boolean.valueOf(isActive.value())); // default defined value is true
                        Attribute id = EventSpecification.findSpecificationByName(AttributeName.TENANT_ID.name(), spec);
                        Assertions.assertNotNull(id.value());// Defined tenant identifier

                        // Read rejection cause
                        Attribute causeAtt = EventSpecification.findSpecificationByName(org.cybnity.framework.domain.event.AttributeName.OUTPUT_CAUSE_TYPE.name(), spec);
                        Assertions.assertEquals(ApplicationServiceOutputCause.EXISTING_TENANT_ALREADY_ASSIGNED.name(), causeAtt.value()); // Defined cause of rejection regarding the active operational status

                        acceptancesCriteriaCheckResultsWaiter.countDown(); // Confirm acceptance treated
                    }
                }
            }
        });
        // Prepare and execute a tenant registration (simulating a first creation already performed by a start of company's platform instance)
        Command cmd = TenantMockHelper.prepareRegisterTenantCommand(organizationName, /*Simulate that it is an operational status already defined as activated for registered users*/ Boolean.TRUE);

        // Submit to registration service
        this.tenantRegistrationService.handle(cmd); // Tenant shall have been added into the events store

        // Prepare and try to execute tenant registration with same name (simulating company's platform instance re-start)
        Command cmd2 = TenantMockHelper.prepareRegisterTenantCommand(organizationName, /* Simulation attempt to force the requested creation of no active tenant creation */ Boolean.FALSE);

        this.uisClient.subscribe(outputObservers, mapperFactory.getMapper(String.class, IDescribed.class));

        // Attempt to create a new tenant with the same name that shall be rejected for existing cause
        this.tenantRegistrationService.handle(cmd2); // Tenant shall have not been added into the events store

        // Wait for give time to message to be processed
        Assertions.assertTrue(acceptancesCriteriaCheckResultsWaiter.await(300, TimeUnit.SECONDS), "Timeout reached before collaboration messages treated!");
        // Remove observers from the channels provider
        this.uisClient.unsubscribe(outputObservers);
    }
}
