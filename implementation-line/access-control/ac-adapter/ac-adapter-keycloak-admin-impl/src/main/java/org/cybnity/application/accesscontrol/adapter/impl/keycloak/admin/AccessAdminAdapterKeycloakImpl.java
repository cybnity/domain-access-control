package org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin;

import org.cybnity.application.accesscontrol.adapter.api.admin.IAccessAdminAdapter;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;

import java.util.logging.Logger;

/**
 * Contract relative to access capabilities administration (e.g setting of system's client scopes, access control configuration supervision).
 * For example, services allowing realms, access workflows, standardized roles management according to privileged capabilities.
 * The features supported by this adapter implementation class are focused on administration of User Access Management's elements.
 */
public class AccessAdminAdapterKeycloakImpl implements IAccessAdminAdapter {

    /**
     * Current context of adapter runtime.
     */
    private final IContext context;

    /**
     * Technical logging
     */
    private static final Logger logger = Logger.getLogger(AccessAdminAdapterKeycloakImpl.class.getName());

    /**
     * Utility class managing the verification of operable adapter instance.
     */
    private ExecutableAdminAdapterChecker healthyChecker;

    /**
     * Default constructor of the adapter ready to manage interactions with a Keycloak instance(s).
     *
     * @param context Mandatory context provider of reusable configuration allowing
     *                to connect to instance(s).
     * @throws IllegalArgumentException    When mandatory parameter is missing.
     * @throws UnoperationalStateException When any required environment variable is
     *                                     not defined or have not value ready for
     *                                     use.
     */
    public AccessAdminAdapterKeycloakImpl(IContext context) throws IllegalArgumentException, UnoperationalStateException {
        if (context == null)
            throw new IllegalArgumentException("Context parameter is required!");
        this.context = context;

        // Check the minimum required data allowing connection to the targeted Redis
        // server
        checkHealthyState();
    }

    @Override
    public void freeUpResources() {

    }

    @Override
    public void checkHealthyState() throws UnoperationalStateException {
        if (healthyChecker == null)
            healthyChecker = new ExecutableAdminAdapterChecker(context);
        // Execution the health check
        healthyChecker.checkOperableState();
    }
}
