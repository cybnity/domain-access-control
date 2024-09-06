package org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin;

import org.cybnity.application.accesscontrol.adapter.api.admin.ISSOAdminAdapter;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;

import java.util.logging.Logger;

/**
 * Contract regarding administration of Single-Sign On (SSO) capabilities that support users or systems on the User Identity and Access Management (UIAM).
 * These are administration and supervision features regarding UIAM solution.
 * The features supported by this adapter implementation class are focused on SSO elements administration.
 */
public class SSOAdminAdapterKeycloakImpl implements ISSOAdminAdapter {

    /**
     * Current context of adapter runtime.
     */
    private final IContext context;

    /**
     * Technical logging
     */
    private static final Logger logger = Logger.getLogger(SSOAdminAdapterKeycloakImpl.class.getName());

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
    public SSOAdminAdapterKeycloakImpl(IContext context) throws IllegalArgumentException, UnoperationalStateException {
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
