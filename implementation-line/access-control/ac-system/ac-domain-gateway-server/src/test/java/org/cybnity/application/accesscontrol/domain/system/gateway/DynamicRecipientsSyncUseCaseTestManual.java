package org.cybnity.application.accesscontrol.domain.system.gateway;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.cybnity.application.accesscontrol.translator.ui.api.ACDomainMessageMapperFactory;
import org.cybnity.application.accesscontrol.translator.ui.api.UICapabilityChannel;
import org.cybnity.application.accesscontrol.ui.api.experience.ExecutionResource;
import org.cybnity.feature.accesscontrol.domain.system.AccessControlDomainProcessModule;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.application.vertx.common.event.AttributeName;
import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.IDescribed;
import org.cybnity.framework.domain.IPresenceObservability;
import org.cybnity.framework.domain.event.CollaborationEventType;
import org.cybnity.framework.domain.event.EventSpecification;
import org.cybnity.framework.domain.event.ProcessingUnitPresenceAnnounced;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.*;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.UISAdapterRedisImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Use case tests regarding the automatic feeding, refresh, clean and re-establishment of routing paths between IO Gateway and feature processing unit.
 */
@ExtendWith({VertxExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class DynamicRecipientsSyncUseCaseTestManual extends CustomContextualizedTest {

    private Thread gatewayModule, processModule;

    private CountDownLatch waiter;

    /**
     * Identifiers of deployment
     */
    private String processModuleId;

    private UISAdapter uisClient;

    /**
     * Domain gateway control channel receiving the announced routing paths to recipients.
     */
    private final Channel domainIOGatewayControlChannel = new Channel(UICapabilityChannel.access_control_pu_presence_announcing.shortName());

    /**
     * Domain gateway channel where registered routing paths as recipients changes are notified.
     */
    private final Channel domainIOGatewayRecipientsListChangesNotificationChannel = new Channel(UICapabilityChannel.access_control_io_gateway_dynamic_routing_plan_evolution.shortName());

    private static final String GATEWAY_SERVICE_NAME = NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.GATEWAY, /* domainName */ "ac", /* componentMainFunction */"io",/* resourceType */ null, /* segregationLabel */ null);
    private static final String FEATURE_SERVICE_NAME = NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.PIPELINE, /* domainName */ "ac", /* componentMainFunction */"tenant_registration",/* resourceType */ ExecutionResource.PROCESSING_UNIT.label(), /* segregationLabel */ null);

    /**
     * Default constructor.
     */
    public DynamicRecipientsSyncUseCaseTestManual() throws UnoperationalStateException {
        super(true, true, false, false, /* With snapshots management capability activated */ true);
    }

    @BeforeEach
    public void initThreads(Vertx vertx) throws Exception {
        // Initialize an adapter connected to contextualized Redis server (Users Interactions Space)
        uisClient = new UISAdapterRedisImpl(context());
        // Executed activities counter
        waiter = new CountDownLatch(2 /* Quantity of started modules to wait before test execution */);

        // Prepare domain capability process module executable instance
        processModule = new Thread(() -> {
            // Start a feature processing unit
            vertx.deployVerticle(AccessControlDomainProcessModule.class.getName()).onSuccess(id2 -> {
                processModuleId = id2;
                logger.fine("Domain capabilities Process Module is started and should have registered its routing paths in AC domain IO gateway");
                waiter.countDown(); // Confirm finalized preparation
            });
        });

        // Prepare gateway module executable instance
        gatewayModule = new Thread(() -> {
            // Start domain IO Gateway
            vertx.deployVerticle(AccessControlDomainIOGateway.class.getName()).onSuccess(id -> {
                logger.fine("Domain IO Gateway is started without recipient lists defined");
                waiter.countDown(); // Confirm finalized preparation
            });
        });
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
     * Check that event is containing acceptable value and types.
     *
     * @param presenceAnnounceEvent To check.
     * @param waiter                To count down.
     */
    private void isValidPUPresenceAnnounced(IDescribed presenceAnnounceEvent, CountDownLatch waiter) {
        Assertions.assertTrue(presenceAnnounceEvent instanceof ProcessingUnitPresenceAnnounced, "Process module should have announced over a standardized event type!");
        ProcessingUnitPresenceAnnounced evt = (ProcessingUnitPresenceAnnounced) presenceAnnounceEvent;
        Assertions.assertNotNull(evt.serviceName());
        Assertions.assertFalse(evt.eventsRoutingPaths().isEmpty(), "Minimum one pipeline entrypoint route shall have been announced!");
        Assertions.assertNotNull(evt.correlationId().value(), "Shall be defined by default by the announcer");
        waiter.countDown();// Notify the received process module announce for recipient update request
    }

    /**
     * Check that event is containing acceptable value and types.
     *
     * @param presenceDeclarationResultEvent To check.
     * @param waiter                         To count down.
     */
    private void isValidRoutingPathsRegisteredConfirmation(IDescribed presenceDeclarationResultEvent, CountDownLatch waiter) {
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

        waiter.countDown();// Notify the received gateway module updated recipients list
    }

    /**
     * Check that event is containing acceptable value and types.
     *
     * @param presenceDeclarationResultEvent To check.
     * @param waiter                         To count down.
     */
    private void isValidRoutingPathsRegistrationRequest(IDescribed presenceDeclarationResultEvent, CountDownLatch waiter) {
        Assertions.assertEquals(CollaborationEventType.PROCESSING_UNIT_PRESENCE_ANNOUNCE_REQUESTED.name(), presenceDeclarationResultEvent.type().value(), "Gateway module should have requested routing plan update to other feature modules over a standardized event type!");
        Collection<Attribute> att = presenceDeclarationResultEvent.specification();
        Attribute nameAttr = EventSpecification.findSpecificationByName(AttributeName.ServiceName.name(), att);
        Assertions.assertTrue(nameAttr != null && GATEWAY_SERVICE_NAME.equals(nameAttr.value()));

        // correlation id
        Attribute corAtr = EventSpecification.findSpecificationByName(Command.CORRELATION_ID, att);
        Assertions.assertTrue(corAtr != null && !corAtr.value().isEmpty(), "Shall be defined by default by the gateway");

        // origin path registration
        Attribute presenceStatus = EventSpecification.findSpecificationByName(ProcessingUnitPresenceAnnounced.SpecificationAttribute.PRESENCE_STATUS.name(), att);
        Assertions.assertTrue(presenceStatus != null && !presenceStatus.value().isEmpty(), "Shall be notified by default by the gateway");

        waiter.countDown();// Notify the received gateway module registration request
    }

    /**
     * Test that a feature processing unit make automatic routing paths registration to its domain IO gateway when it is started.
     * The test start firstly an IO gateway instance, and start a process module (normally registering routes to gateway), and attempt to send of event supported by routing plan to process module over gateway.
     */
    @Test
    public void givenStartedGateway_whenFeaturePUStart_thenRecipientsListRegistered(Vertx vertx, VertxTestContext testContext) throws Exception {
        Collection<ChannelObserver> dynamicCollaborationChannelsObservers = new ArrayList<>();
        try {
            // Prepare helper for test time wait regarding all presence announced
            // --- process module tenant registration processing unit pipeline
            // --- gateway module io pipeline
            final CountDownLatch testWaiter = new CountDownLatch(2 /* Qty of presences of process module included */);

            // Prepare a listener of features process module's announces like normally made by the domain gateway (simulate gateway server-side)
            final ChannelObserver processModuleAnnouncesListener = new ProcessModuleAnnouncesListener(testWaiter);

            // Prepare a listener of the confirmed recipients changes like normally made by the process module (simulate process server-side)
            final ChannelObserver gatewayRoutingPlanChangesListener = new GatewayRoutingPlanChangesListener(testWaiter);

            // Define test specific observers set
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
            Assertions.assertTrue(testWaiter.await(420, TimeUnit.SECONDS), "Timeout reached before collaboration messages treated!");
        } finally {
            // Unregister specific test listeners started
            uisClient.unsubscribe(dynamicCollaborationChannelsObservers);
        }
        testContext.completeNow();
    }

    /**
     * Test that a previous registered routing plan regarding an available Feature PU, is automatically notified as unavailable into a Gateway when the Feature PU is stopped.
     */
    @Test
    public void givenActiveFeaturePUWithActiveRegisteredRoutingPaths_whenPUStopped_thenUnavailabilityAnnounceCollectedByGateway(Vertx vertx, VertxTestContext testContext) throws Exception {
        Collection<ChannelObserver> dynamicCollaborationChannelsObservers = new ArrayList<>();
        try {
            // Prepare helper for test time wait regarding unavailability notification
            // --- gateway module io pipeline started
            // --- process module tenant registration processing unit pipeline started
            // --- process module PU stopped
            CountDownLatch testWaiter = new CountDownLatch(3);

            // Prepare a listener of features process module announced
            final ChannelObserver processModuleAnnouncesListener = new ProcessModuleAnnouncesListener(testWaiter);

            // Prepare a listener of the confirmed recipients changes
            final ChannelObserver gatewayRoutingPlanChangesListener = new GatewayRoutingPlanChangesListener(testWaiter);

            final Runnable action = () -> {
                // Execute the stop of the module by undeploy process
                vertx.undeploy(processModuleId);
                processModuleId = null;
            };

            // Prepare a listener of the notified stopped feature normally notifying the gateway module
            final ChannelObserver featureModuleStopListener = new ChannelObserver() {
                @Override
                public Channel observed() {
                    return domainIOGatewayControlChannel;
                }

                @Override
                public String observationPattern() {
                    return null;
                }

                @Override
                public void notify(Object event) {
                    if (event != null && IDescribed.class.isAssignableFrom(event.getClass())) {
                        IDescribed presenceAnnounceEvent = (IDescribed) event;
                        // Process module announce have been received
                        logger.fine("--- Presence event: " + presenceAnnounceEvent.type().value());

                        // --- CHECK VALID ANNOUNCE ABOUT UNAVAILABILITY ---
                        Assertions.assertTrue(presenceAnnounceEvent instanceof ProcessingUnitPresenceAnnounced, "Should be announced over a standardized event type!");
                        ProcessingUnitPresenceAnnounced evt = (ProcessingUnitPresenceAnnounced) presenceAnnounceEvent;
                        Assertions.assertNotNull(evt.serviceName());

                        // Detect if it's a module start or end of operational status
                        if (IPresenceObservability.PresenceState.AVAILABLE.name().equals(evt.presenceStatus().value())) {
                            Collection<Attribute> att = presenceAnnounceEvent.specification();
                            Attribute nameAttr = EventSpecification.findSpecificationByName(ProcessingUnitPresenceAnnounced.SpecificationAttribute.SERVICE_NAME.name(), att);
                            // Considerate only feature processing unit
                            if (nameAttr != null && FEATURE_SERVICE_NAME.equals(nameAttr.value())) {
                                // Now undeploy the confirmed started PU
                                // via stop of module
                                action.run();
                            }
                        } else {
                            // Detect that event is about a stopped module

                            // Confirm the success received announce of presence end
                            testWaiter.countDown();
                        }
                    }
                }
            };

            // Define test specific observers set
            dynamicCollaborationChannelsObservers.add(processModuleAnnouncesListener);
            dynamicCollaborationChannelsObservers.add(gatewayRoutingPlanChangesListener);
            dynamicCollaborationChannelsObservers.add(featureModuleStopListener);

            // Register all consumers of observed channels
            uisClient.subscribe(dynamicCollaborationChannelsObservers, messageMapperProvider().getMapper(String.class, IDescribed.class));

            // --- Start GATEWAY FIRSTLY
            gatewayModule.start();
            gatewayModule.join(); // wait end of gateway start execution
            // --- Start PROCESS MODULE SECONDLY
            processModule.start();
            processModule.join(); // wait end of process start execution

            // Wait for give time to message to be processed
            Assertions.assertTrue(testWaiter.await(420, TimeUnit.SECONDS), "Timeout reached before collaboration messages treated!");
        } finally {
            // Unregister specific test listeners started
            uisClient.unsubscribe(dynamicCollaborationChannelsObservers);
        }
        testContext.completeNow();
    }

    /**
     * When a Feature PU is started BEFORE a Gateway (e.g period of temporary unavailability during a restart in progress),
     * a notification of routing paths registration is automatically requested by the Gateway's start process,
     * and the recipients list are re-established since a new received announce from Feature PU always available.
     */
    @Test
    public void givenFeaturePUStartedBeforeGateway_whenGatewayStarted_thenAutoReRegistrationAttemptSuccessfullyEstablishedByGateway(Vertx vertx, VertxTestContext testContext) throws Exception {
        Collection<ChannelObserver> dynamicCollaborationChannelsObservers = new ArrayList<>();
        try {
            // Prepare helper for test time wait regarding unavailability notification
            // --- process module tenant registration processing unit pipeline started
            // --- gateway module io pipeline started and gateway module auto-requesting new routing paths refresh
            CountDownLatch testWaiter = new CountDownLatch(2);

            // Prepare a listener of features process module announced
            final ChannelObserver processModuleAnnouncesListener = new ProcessModuleAnnouncesListener(testWaiter);

            // Prepare a listener of the confirmed recipients changes
            final ChannelObserver gatewayRoutingPlanChangesListener = new GatewayRoutingPlanChangesListener(testWaiter);

            // Define test specific observers set
            dynamicCollaborationChannelsObservers.add(processModuleAnnouncesListener);
            dynamicCollaborationChannelsObservers.add(gatewayRoutingPlanChangesListener);

            // Register all consumers of observed channels
            uisClient.subscribe(dynamicCollaborationChannelsObservers, messageMapperProvider().getMapper(String.class, IDescribed.class));

            // --- Start FEATURE PROCESS MODULE FIRSTLY
            processModule.start();
            processModule.join(); // wait end of process start execution
            // --- Start GATEWAY MODULE SECONDLY
            gatewayModule.start();
            gatewayModule.join(); // wait end of gateway start execution

            // Wait for give time to message to be processed
            Assertions.assertTrue(testWaiter.await(420, TimeUnit.SECONDS), "Timeout reached before collaboration messages treated!");
        } finally {
            // Unregister specific test listeners started
            uisClient.unsubscribe(dynamicCollaborationChannelsObservers);
        }
        testContext.completeNow();
    }

    private class ProcessModuleAnnouncesListener implements ChannelObserver {
        CountDownLatch testWaiter;

        public ProcessModuleAnnouncesListener(CountDownLatch testWaiter) {
            this.testWaiter = testWaiter;
        }

        @Override
        public Channel observed() {
            return domainIOGatewayControlChannel;
        }

        @Override
        public String observationPattern() {
            return null;
        }

        @Override
        public void notify(Object evt) {
            if (evt != null && IDescribed.class.isAssignableFrom(evt.getClass())) {
                try {
                    IDescribed presenceAnnounceEvent = (IDescribed) evt;
                    logger.fine("--- Presence event: " + presenceAnnounceEvent.type().value());
                    // Check conformity of event
                    isValidPUPresenceAnnounced(presenceAnnounceEvent, this.testWaiter);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Invalid event description!", e);
                }
            }
        }
    }

    private class GatewayRoutingPlanChangesListener implements ChannelObserver {
        CountDownLatch testWaiter;

        public GatewayRoutingPlanChangesListener(CountDownLatch testWaiter) {
            this.testWaiter = testWaiter;
        }

        @Override
        public Channel observed() {
            return domainIOGatewayRecipientsListChangesNotificationChannel;
        }

        @Override
        public String observationPattern() {
            return null;
        }

        @Override
        public void notify(Object evt) {
            if (evt != null && IDescribed.class.isAssignableFrom(evt.getClass())) {
                try {
                    IDescribed presenceDeclarationResultEvent = (IDescribed) evt;
                    logger.fine("--- Routing plan update event: " + presenceDeclarationResultEvent.type().value());
                    // Check event conformity
                    if (CollaborationEventType.PROCESSING_UNIT_ROUTING_PATHS_REGISTERED.name().equals(presenceDeclarationResultEvent.type().value())) {
                        // Verify the description of the registration confirmation event
                        isValidRoutingPathsRegisteredConfirmation(presenceDeclarationResultEvent, this.testWaiter);
                    } else if (CollaborationEventType.PROCESSING_UNIT_PRESENCE_ANNOUNCE_REQUESTED.name().equals(presenceDeclarationResultEvent.type().value())) {
                        // Verify the description of the requested re-registration demand
                        isValidRoutingPathsRegistrationRequest(presenceDeclarationResultEvent, this.testWaiter);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Invalid event description!", e);
                }
            }
        }
    }

}
