package org.cybnity.application.accesscontrol.domain.system.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.cybnity.application.accesscontrol.domain.system.gateway.AccessControlDomainIOGateway;
import org.cybnity.application.accesscontrol.domain.system.gateway.ContextualizedTest;
import org.cybnity.feature.accesscontrol.domain.system.AccessControlDomainProcessModule;
import org.cybnity.framework.domain.ObjectMapperBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

/**
 * Use case tests regarding the automatic feeding, refresh, clean and re-establishment of routing paths between IO Gateway and feature processing unit.
 */
@ExtendWith({VertxExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class DynamicRecipientsSyncUseCaseTest extends ContextualizedTest {

    /**
     * Test that a feature processing unit make automatic routing paths registration to its domain IO gateway when it is started.
     * The test start firstly an IO gateway instance, and start a process module (normally registering routes to gateway), and attempt to send of event supported by routing plan to process module over gateway.
     */
    @Test
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    public void givenStartedGateway_whenFeaturePUStart_thenRecipientsListRegistered(Vertx vertx, VertxTestContext testContext) throws Exception {
        // Start domain IO Gateway
        vertx.deployVerticle(AccessControlDomainIOGateway.class.getName()).onSuccess(id -> {
            logger.fine("Domain IO Gateway started");

            // Start a feature processing unit
            vertx.deployVerticle(AccessControlDomainProcessModule.class.getName()).onSuccess(id2 -> {
                logger.fine("Feature process module started");

                // Try to send supported event type to gateway

                // Confirm finalized test
                testContext.completeNow();
            });
        });
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
