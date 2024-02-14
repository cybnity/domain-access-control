package org.cybnity.accesscontrol.domain.service.impl;

import org.cybnity.accesscontrol.domain.service.TenantRegistrationServiceConfigurationVariable;
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
 * configuration and contents allowing runnable service component.
 *
 * @author olivier
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_8370_CM6")
public class ExecutableTenantRegistrationServiceChecker extends ExecutableComponentChecker {

    /**
     * Constructor with dedicated context to use by this checker.
     *
     * @param ctx A context or null.
     */
    public ExecutableTenantRegistrationServiceChecker(IContext ctx) {
        super(ctx);
    }

    /**
     * Default constructor.
     */
    public ExecutableTenantRegistrationServiceChecker() {
        super();
    }

    @Override
    public Set<IReadableConfiguration> requiredEnvironmentVariables() {
        // Define the mandatory environment variables
        // - variables required for service component
        HashSet<IReadableConfiguration> variables = new HashSet<>(EnumSet.allOf(TenantRegistrationServiceConfigurationVariable.class));

        // Remove optional variables
        variables.removeAll(optionalEnvironmentVariables());

        return variables;
    }

    /**
     * Get the environment variables that are not mandatory for the component running but that can be not defined.
     *
     * @return A set of optional variables.
     */
    public Set<IReadableConfiguration> optionalEnvironmentVariables() {
        // Define the optional environment variables for service running
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