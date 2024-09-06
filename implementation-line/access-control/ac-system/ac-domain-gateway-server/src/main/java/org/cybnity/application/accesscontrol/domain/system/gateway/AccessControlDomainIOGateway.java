package org.cybnity.application.accesscontrol.domain.system.gateway;

import io.vertx.core.Vertx;
import org.cybnity.application.accesscontrol.domain.system.gateway.service.DomainIOEventsPipeline;
import org.cybnity.framework.application.vertx.common.module.AbstractProcessModuleImpl;
import org.cybnity.framework.immutable.utility.ExecutableComponentChecker;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.NamingConventionHelper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Start a composition of gateway Verticle supporting the identification of command supported by this domain and distribution to processing units (e.g UI capability)
 * and ensuring the forwarding of domain events to UI layer (e.g domain reactive messaging gateway).
 */
public class AccessControlDomainIOGateway extends AbstractProcessModuleImpl {

    /**
     * Technical logging
     */
    private static final Logger logger = Logger.getLogger(AccessControlDomainIOGateway.class.getName());

    /**
     * Default start method regarding the server.
     *
     * @param args None pre-required.
     */
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        // Deploy health check support over http
        vertx.deployVerticle(new AccessControlDomainIOGateway()).onComplete(res -> {
            if (res.succeeded()) {
                logger.info("Access control (AC) domain IO Gateway deployed (id: " + res.result() + ")");
            } else {
                logger.info("Access control (AC) domain IO Gateway deployment failed!");
            }
        });
    }

    @Override
    protected ExecutableComponentChecker healthyChecker() {
        return new ExecutableIOGatewayChecker(workersCapability().context);
    }

    @Override
    public String poolName() {
        return NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.GATEWAY, /* domainName */ "ac", /* componentMainFunction */"io",/* resourceType */ null, /* segregationLabel */ "workers");
    }

    @Override
    protected Logger logger() {
        return logger;
    }

    @Override
    public String featuresDomainName() {
        return "AC IO";
    }

    @Override
    public String processUnitLogicalName() {
        return featuresDomainName() + " Gateway";
    }

    @Override
    protected Collection<Class<?>> deployedWorkers() {
        // --- EMBEDDED DOMAIN FEATURES DEFINITION ---
        LinkedList<Class<?>> featureWorkerTypes = new LinkedList<>();

        // Add each pipelined IO component type to the set of workers serving capabilities API with or without access control check
        featureWorkerTypes.add(DomainIOEventsPipeline.class);

        return featureWorkerTypes;
    }

    @Override
    public void freeUpResources() {

    }
}
