package org.cybnity.application.accesscontrol.domain.system.gateway;

import org.cybnity.framework.IContext;
import org.cybnity.framework.IReadableConfiguration;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.application.vertx.common.AppConfigurationVariable;
import org.cybnity.framework.immutable.utility.ExecutableComponentChecker;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation class regarding the verification of minimum required
 * configuration and contents allowing runnable gateway.
 *
 * @author olivier
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_8370_CM6")
public class ExecutableIOGatewayChecker extends ExecutableComponentChecker {

    /**
     * Constructor with dedicated context to use by this checker.
     *
     * @param ctx A context or null.
     */
    public ExecutableIOGatewayChecker(IContext ctx) {
        super(ctx);
    }

    /**
     * Default constructor.
     */
    public ExecutableIOGatewayChecker() {
        super();
    }

    @Override
    public Set<IReadableConfiguration> requiredEnvironmentVariables() {
        // Define the mandatory environment variable for backend running
        // - common variables required for application module
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
        // Define the optional environment variables for gateway running
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