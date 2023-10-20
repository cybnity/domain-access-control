package org.cybnity.application.accesscontrol.ui.system.backend;

import org.cybnity.framework.IContext;
import org.cybnity.framework.IReadableConfiguration;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.immutable.utility.ExecutableComponentChecker;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation class regarding the verification of minimum required
 * configuration and contents allowing runnable adapter.
 *
 * @author olivier
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_8370_CM6")
public class ExecutableBackendChecker extends ExecutableComponentChecker {

    /**
     * Constructor with dedicated context to use by this checker.
     *
     * @param ctx A context or null.
     */
    public ExecutableBackendChecker(IContext ctx) {
        super(ctx);
    }

    /**
     * Default constructor.
     */
    public ExecutableBackendChecker() {
        super();
    }

    @Override
    protected Set<IReadableConfiguration> requiredEnvironmentVariables() {
        // Define the mandatory environment variable for backend running
        // - required for application module
        HashSet<IReadableConfiguration> variables = new HashSet<>(EnumSet.allOf(AppConfigurationVariable.class));

        // Remove optional variables
        variables.removeAll(optionalEnvironmentVariables());

        return variables;
    }

    /**
     * Get the environment variables that are not mandatory for the backend running but that can be not defined.
     *
     * @return A set of optional variables.
     */
    public Set<IReadableConfiguration> optionalEnvironmentVariables() {
        // Define the optional environment variables for backend running
        HashSet<IReadableConfiguration> variables = new HashSet<>();
        variables.add(AppConfigurationVariable.AUTHORIZED_WHITE_LIST_ORIGIN_SERVER_URLS);
        return variables;
    }

    @Override
    protected void checkOperatingFiles() throws UnoperationalStateException {

    }

    @Override
    protected void checkResourcesPermissions() throws UnoperationalStateException {

    }

}