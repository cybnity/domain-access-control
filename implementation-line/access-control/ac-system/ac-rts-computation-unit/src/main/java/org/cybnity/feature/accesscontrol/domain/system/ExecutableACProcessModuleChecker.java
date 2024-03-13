package org.cybnity.feature.accesscontrol.domain.system;

import org.cybnity.accesscontrol.domain.service.impl.ExecutableTenantRegistrationServiceChecker;
import org.cybnity.framework.IContext;
import org.cybnity.framework.IReadableConfiguration;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.application.vertx.common.AppConfigurationVariable;
import org.cybnity.framework.immutable.utility.ExecutableComponentChecker;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

import java.util.*;

/**
 * Implementation class regarding the verification of minimum required
 * configuration and contents allowing runnable AC process module.
 * It includes the checker regarding the services components reused by the module in an embedded approach, allowing to verify also their environment variables during this module operational state control.
 *
 * @author olivier
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_8370_CM6")
public class ExecutableACProcessModuleChecker extends ExecutableComponentChecker {

    /**
     * Constructor with dedicated context to use by this checker.
     *
     * @param ctx A context or null.
     */
    public ExecutableACProcessModuleChecker(IContext ctx) {
        super(ctx);
    }

    /**
     * Default constructor.
     */
    public ExecutableACProcessModuleChecker() {
        super();
    }

    @Override
    public Set<IReadableConfiguration> requiredEnvironmentVariables() {
        // Define the mandatory environment variables
        // - common variables required for application module
        HashSet<IReadableConfiguration> variables = new HashSet<>(EnumSet.allOf(AppConfigurationVariable.class));


        // --- OPERATIONAL STATE CHECKER DEFINITION ---
        Collection<ExecutableComponentChecker> additionalCheckers = workersCheckers();
        for (ExecutableComponentChecker workerChecker : additionalCheckers) {
            // Add environment variable required by the worker (already cleaned about optional variables)
            variables.addAll(workerChecker.requiredEnvironmentVariables());
        }

        // Remove optional variables
        variables.removeAll(optionalEnvironmentVariables());

        return variables;
    }

    /**
     * Get the operational status check capabilities regarding the deployed workers.
     *
     * @return Checkers list supporting workers as defined by AccessControlDomainProcessModule.deployedWorkers() method.
     */
    private Collection<ExecutableComponentChecker> workersCheckers() {
        Collection<ExecutableComponentChecker> workersOperationalCheckers = new ArrayList<>();

        // --- Add required worker configuration environment variables based on each worker defined by AccessControlDomainProcessModule.deployedWorkers() method ---
        // Set the Tenant registration service checker
        workersOperationalCheckers.add(new ExecutableTenantRegistrationServiceChecker(getContext()));

        return workersOperationalCheckers;
    }

    /**
     * Get the environment variables that are not mandatory for the module running but that can be not defined.
     *
     * @return A set of optional variables.
     */
    public Set<IReadableConfiguration> optionalEnvironmentVariables() {
        // Define the optional environment variables for module running
        HashSet<IReadableConfiguration> variables = new HashSet<>();
        return variables;
    }

    @Override
    protected void checkOperatingFiles() throws UnoperationalStateException {

    }

    @Override
    protected void checkResourcesPermissions() throws UnoperationalStateException {

    }

}