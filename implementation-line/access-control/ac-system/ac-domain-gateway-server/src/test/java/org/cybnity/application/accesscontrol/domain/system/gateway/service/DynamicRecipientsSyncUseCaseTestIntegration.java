package org.cybnity.application.accesscontrol.domain.system.gateway.service;

import io.vertx.core.Vertx;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.cybnity.application.accesscontrol.domain.system.gateway.AccessControlDomainIOGateway;
import org.cybnity.application.accesscontrol.domain.system.gateway.ContextualizedTest;
import org.cybnity.application.accesscontrol.translator.ui.api.ACDomainMessageMapperFactory;
import org.cybnity.application.accesscontrol.ui.api.UICapabilityChannel;
import org.cybnity.feature.accesscontrol.domain.system.AccessControlDomainProcessModule;
import org.cybnity.framework.application.vertx.common.event.AttributeName;
import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.IDescribed;
import org.cybnity.framework.domain.IPresenceObservability;
import org.cybnity.framework.domain.event.CollaborationEventType;
import org.cybnity.framework.domain.event.EventSpecification;
import org.cybnity.framework.domain.event.ProcessingUnitPresenceAnnounced;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.*;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.UISAdapterImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Use case tests regarding the automatic feeding, refresh, clean and re-establishment of routing paths between IO Gateway and feature processing unit.
 */
@ExtendWith({VertxExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class DynamicRecipientsSyncUseCaseTestIntegration extends ContextualizedTest {

    private Thread gatewayModule, processModule;
    private CountDownLatch waiter;
    /**
     * Identifiers of deployment
     */
    private String gatewayModuleId, processModuleId;

    private UISAdapter uisClient;

    /**
     * Domain gateway control channel receiving the announced routing paths to recipients.
     */
    private Channel domainIOGatewayControlChannel = new Channel(UICapabilityChannel.access_control_pu_presence_announcing.shortName());

    /**
     * Domain gateway channel where registered routing paths as recipients changes are notified.
     */
    private Channel domainIOGatewayRecipientsListChangesNotificationChannel = new Channel(UICapabilityChannel.access_control_io_gateway_dynamic_routing_plan_evolution.shortName());

    private static final String GATEWAY_SERVICE_NAME = NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.GATEWAY, /* domainName */ "ac", /* componentMainFunction */"io",/* resourceType */ null, /* segregationLabel */ null);


    @BeforeEach
    public void initThreads(Vertx vertx) throws Exception {
        // Executed activities counter
        waiter = new CountDownLatch(2 /* Quantity of message to wait about processing end confirmation */);
        // Prepare gateway module executable instance
        gatewayModule = new Thread(() -> {
            // Start domain IO Gateway
            vertx.deployVerticle(AccessControlDomainIOGateway.class.getName()).onSuccess(id -> {
                gatewayModuleId = id;
                logger.fine("Domain IO Gateway is started without recipient lists defined");
                waiter.countDown(); // Confirm finalized preparation
            });
        });
        // Prepare domain capability process module executable instance
        processModule = new Thread(() -> {
            // Start a feature processing unit
            vertx.deployVerticle(AccessControlDomainProcessModule.class.getName()).onSuccess(id2 -> {
                logger.fine("Domain capabilities Process Module is started and should have registered its routing paths in AC domain IO gateway");
                processModuleId = id2;
                waiter.countDown(); // Confirm finalized preparation
            });
        });

        // Initialize an adapter connected to contextualized Redis server (Users Interactions Space)
        uisClient = new UISAdapterImpl(getContext());
    }

    /**
     * Stop and undeploy the prepared/started vertx
     */
    @AfterEach
    public void stopThreads(Vertx vertx) {
        // Undeploy the started vertx modules
        if (processModuleId != null && !processModuleId.isEmpty()) vertx.undeploy(processModuleId);
        if (gatewayModuleId != null && !gatewayModuleId.isEmpty()) vertx.undeploy(gatewayModuleId);
    }

    /**
     * Get a domain mapper usable for message interpretation.
     *
     * @return A factory instance.
     */
    private IMessageMapperProvider messageMapperProvider() {
        return new ACDomainMessageMapperFactory();
    }

    /**
     * Test that a feature processing unit make automatic routing paths registration to its domain IO gateway when it is started.
     * The test start firstly an IO gateway instance, and start a process module (normally registering routes to gateway), and attempt to send of event supported by routing plan to process module over gateway.
     */
    @Test
    @Timeout(value = 80, timeUnit = TimeUnit.SECONDS)
    public void givenStartedGateway_whenFeaturePUStart_thenRecipientsListRegistered(Vertx vertx, VertxTestContext testContext) throws Exception {
        // Prepare helper for test time wait regarding all presence announced
        // --- process module tenant registration processing unit pipeline
        // --- gateway module io pipeline
        CountDownLatch testWaiter = new CountDownLatch(2 /* Qty of presences of process module included */);

        // Prepare a listener of features process module announces like normally made by the domain gateway (simulate gateway server-side)
        ChannelObserver processModuleAnnouncesListener = new ChannelObserver() {
            @Override
            public Channel observed() {
                return domainIOGatewayControlChannel;
            }

            @Override
            public String observationPattern() {
                return null;
            }

            @Override
            public void notify(IDescribed presenceAnnounceEvent) {
                // Process module announce have been received
                logger.fine("--- Presence event: " + presenceAnnounceEvent.type().value());
                Assertions.assertTrue(presenceAnnounceEvent instanceof ProcessingUnitPresenceAnnounced, "Process module should have announced over a standardized event type!");
                ProcessingUnitPresenceAnnounced evt = (ProcessingUnitPresenceAnnounced) presenceAnnounceEvent;
                Assertions.assertNotNull(evt.serviceName());
                Assertions.assertFalse(evt.eventsRoutingPaths().isEmpty(), "Minimum one pipeline entrypoint route shall have been announced!");
                Assertions.assertEquals(IPresenceObservability.PresenceState.AVAILABLE.name(), evt.presenceStatus().value());
                Assertions.assertNotNull(evt.correlationId().value(), "Shall be defined by default by the announcer");
                testWaiter.countDown();// Notify the received process module announce for recipient update request
            }
        };

        // Prepare a listener of the confirmed recipients changes like normally made by the process module (simulate process server-side)
        ChannelObserver gatewayRoutingPlanChangesListener = new ChannelObserver() {
            @Override
            public Channel observed() {
                return domainIOGatewayRecipientsListChangesNotificationChannel;
            }

            @Override
            public String observationPattern() {
                return null;
            }

            @Override
            public void notify(IDescribed presenceDeclarationResultEvent) {
                // Process module announce have been received
                logger.fine("--- Routing plan update event: " + presenceDeclarationResultEvent.type().value());
                Assertions.assertEquals(CollaborationEventType.PROCESSING_UNIT_ROUTING_PATHS_REGISTERED.name(), presenceDeclarationResultEvent.type().value(), "Gateway module should have confirmed routing plan updated over a standardized event type!");
                Collection<Attribute> att = presenceDeclarationResultEvent.specification();
                Attribute nameAttr = EventSpecification.findSpecificationByName(AttributeName.ServiceName.name(), att);
                Assertions.assertTrue(nameAttr != null && GATEWAY_SERVICE_NAME.equals(nameAttr.value()));

                // correlation id
                Attribute corAtr = EventSpecification.findSpecificationByName(Command.CORRELATION_ID, att);
                Assertions.assertTrue(corAtr != null && !corAtr.value().isEmpty(), "Shall be defined by default by the gateway");

                // origin path registration
                Attribute originAttr = EventSpecification.findSpecificationByName(AttributeName.SourceChannelName.name(), att);
                Assertions.assertTrue(originAttr != null && !originAttr.value().isEmpty(), "Shall be defined by default by the gateway");

                testWaiter.countDown();// Notify the received gateway module updated recipients list
            }
        };

        // Define test specific observers set
        Collection<ChannelObserver> dynamicCollaborationChannelsObservers = new ArrayList<>();
        dynamicCollaborationChannelsObservers.add(processModuleAnnouncesListener);
        dynamicCollaborationChannelsObservers.add(gatewayRoutingPlanChangesListener);
        // Register all consumers of observed channels
        uisClient.subscribe(dynamicCollaborationChannelsObservers, messageMapperProvider().getMapper(String.class, IDescribed.class));

        // Stop ordered modules according to:
        // --- Start GATEWAY FIRSTLY
        gatewayModule.start();
        gatewayModule.join(); // wait end of gateway start execution
        // --- Start PROCESS MODULE SECONDLY
        processModule.start();
        processModule.join(); // wait end of process start execution

        // Wait for give time to message to be processed
        Assertions.assertTrue(testWaiter.await(50, TimeUnit.SECONDS), "Timeout reached before collaboration messages treated!");

        // unregister specific test listeners started
        uisClient.unsubscribe(dynamicCollaborationChannelsObservers);
        dynamicCollaborationChannelsObservers.clear();
        testContext.completeNow();
    }

    /**
     * Test that a previous registered routing plan to an available Feature PU, is automatically notified as unavailable into a Gateway when the feature module is stopped.
     */
    @Test
    public void givenActiveFeaturePUWithActiveRegisteredRoutingPaths_whenPUStopped_thenUnavailabilityAnnounceCollectedByGateway(Vertx vertx, VertxTestContext testContext) throws Exception {
        testContext.completeNow();
    }

    /**
     * When a feature is already started before a Gateway (e.g temporary stopped), a notification of routing paths registration is automatically request by Gateway's start process and recipients list re-established about the Feature PU already available.
     */
    @Test
    public void givenFeaturePUStartedBeforeGateway_whenGatewayStarted_thenAutoReRegistrationAttemptSuccessfullyEstablishedByGateway(Vertx vertx, VertxTestContext testContext) throws Exception {
        testContext.completeNow();
    }
}
