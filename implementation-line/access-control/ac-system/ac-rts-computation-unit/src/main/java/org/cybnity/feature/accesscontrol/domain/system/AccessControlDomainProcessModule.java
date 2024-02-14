package org.cybnity.feature.accesscontrol.domain.system;

import io.vertx.core.Vertx;
import org.cybnity.feature.accesscontrol.domain.system.service.TenantRegistrationFeaturePipeline;
import org.cybnity.framework.application.vertx.common.module.AbstractProcessModuleImpl;
import org.cybnity.framework.immutable.utility.ExecutableComponentChecker;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.NamingConventionHelper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Start a composition of gateway Verticle supporting the application security
 * services provided by the processing unit of the domain.
 * It's a packaged process module ensuring internal start of each Capability feature as independent capability.
 */
public class AccessControlDomainProcessModule extends AbstractProcessModuleImpl {

    /**
     * Dedicated module's technical logging.
     */
    private static final Logger logger = Logger.getLogger(AccessControlDomainProcessModule.class.getName());

    /**
     * Default start method regarding the server.
     *
     * @param args None pre-required.
     */
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        // Deploy health check support over http
        vertx.deployVerticle(new AccessControlDomainProcessModule()).onComplete(res -> {
            if (res.succeeded()) {
                logger.info("Access control (AC) domain Process Module deployed (id: " + res.result() + ")");
            } else {
                logger.info("Access control (AC) domain Process Module deployment failed!");
            }
        });
    }

    /**
     * Get a checker aggregation allowing the check of all the embedded services components (e.g deployed workers) additionally to the default module checker.
     *
     * @return A checker covering this module and embedded services.
     */
    @Override
    protected ExecutableComponentChecker healthyChecker() {
        return new ExecutableACProcessModuleChecker(workersCapability().context);
    }

    @Override
    public String poolName() {
        return NamingConventionHelper.buildComponentName(/* component type */NamingConventionHelper.NamingConventionApplicability.PROCESSING_UNIT,
                /* domainName */ "ac", /* componentMainFunction */"process-module",/* resourceType */ null, /* segregationLabel */ "workers");
    }

    @Override
    protected Logger logger() {
        return logger;
    }

    @Override
    public String featuresDomainName() {
        return "AC Features";
    }

    @Override
    public String processUnitLogicalName() {
        return featuresDomainName() + " Processing Unit";
    }

    /**
     * All the process module integrated features types.
     *
     * @return Access Control domain UI capability features.
     */
    @Override
    protected Collection<Class<?>> deployedWorkers() {
        // --- EMBEDDED DOMAIN FEATURES DEFINITION ---
        LinkedList<Class<?>> featureWorkerTypes = new LinkedList<>();

        // Add each feature worker to the set of workers providing services
        featureWorkerTypes.add(TenantRegistrationFeaturePipeline.class);

        return featureWorkerTypes;
    }

}
