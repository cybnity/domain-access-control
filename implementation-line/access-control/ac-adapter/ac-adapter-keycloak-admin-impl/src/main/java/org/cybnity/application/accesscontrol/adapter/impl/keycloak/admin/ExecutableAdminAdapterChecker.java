package org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin;

import org.cybnity.framework.IContext;
import org.cybnity.framework.IReadableConfiguration;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.immutable.utility.ExecutableComponentChecker;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation class regarding the verification of minimum required
 * configuration and contents allowing runnable adapter.
 *
 * @author olivier
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_8370_CM6")
public class ExecutableAdminAdapterChecker extends ExecutableComponentChecker {

    /**
     * Constructor with dedicated context to use by this checker.
     *
     * @param ctx A context or null.
     */
    public ExecutableAdminAdapterChecker(IContext ctx) {
        super(ctx);
    }

    /**
     * Default constructor.
     */
    public ExecutableAdminAdapterChecker() {
        super();
    }

    @Override
    public Set<IReadableConfiguration> requiredEnvironmentVariables() {
        // Define the mandatory environment variable for adapter running
        Set<IReadableConfiguration> required = new HashSet<>();

        // - required for Keycloak Admin REST API client instantiation
        required.add(AdminConfigurationVariable.REALM_MASTER_NAME);
        required.add(AdminConfigurationVariable.KEYCLOAK_SERVER_URL);
        required.add(AdminConfigurationVariable.REALM_MASTER_USERNAME);
        required.add(AdminConfigurationVariable.REALM_MASTER_PASSWORD);
        required.add(AdminConfigurationVariable.REALM_MASTER_CLIENTID);
        required.add(AdminConfigurationVariable.REALM_MASTER_GRANT_TYPE);

        return required;
    }

    @Override
    protected void checkOperatingFiles() throws UnoperationalStateException {
        // None embedded files need to be check regarding the adapter to remote
        // server
    }

    @Override
    protected void checkResourcesPermissions() throws UnoperationalStateException {
        // None embedded resources into the adapter library that need to be checked in
        // terms of permissions
    }

}
