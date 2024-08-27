package org.cybnity.application.accesscontrol.adapter.impl.keycloak;

import org.cybnity.application.accesscontrol.adapter.api.SSOAdapter;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;

import java.util.logging.Logger;

/**
 * Implementation adapter to a Keycloak server.
 */
public class SSOAdapterKeycloakImpl implements SSOAdapter {

    /**
     * Current context of adapter runtime.
     */
    private final IContext context;

    /**
     * Technical logging
     */
    private static final Logger logger = Logger.getLogger(SSOAdapterKeycloakImpl.class.getName());

    /**
     * Utility class managing the verification of operable adapter instance.
     */
    private ExecutableAdapterChecker healthyChecker;

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
    public SSOAdapterKeycloakImpl(IContext context) throws IllegalArgumentException, UnoperationalStateException {
        if (context == null)
            throw new IllegalArgumentException("Context parameter is required!");
        this.context = context;

        // Check the minimum required data allowing connection to the targeted Redis
        // server
        checkHealthyState();
    }

    @Override
    public void freeResources() {

    }

    @Override
    public void checkHealthyState() throws UnoperationalStateException {
        if (healthyChecker == null)
            healthyChecker = new ExecutableAdapterChecker(context);
        // Execution the health check
        healthyChecker.checkOperableState();
    }
}