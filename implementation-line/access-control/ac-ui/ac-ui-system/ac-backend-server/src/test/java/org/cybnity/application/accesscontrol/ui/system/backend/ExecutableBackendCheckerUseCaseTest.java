package org.cybnity.application.accesscontrol.ui.system.backend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import io.vertx.core.Vertx;
import org.cybnity.framework.IReadableConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

/**
 * Tests regarding the utility class that check the healthy and operational
 * state of a backend runnable.
 *
 * @author olivier
 */
@ExtendWith(SystemStubsExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ExecutableBackendCheckerUseCaseTest {

    @SystemStub
    private EnvironmentVariables environmentVariables;

    public void initMinimumRequiredEnvVariables() {
        // Define environment variables in execution context

        // All environment variables
        for (IReadableConfiguration aReq : EnumSet.allOf(AppConfigurationVariable.class)) {
            environmentVariables.set(aReq.getName(),
                    /* Insert random value as variable value */ UUID.randomUUID().toString());
        }
    }

    /**
     * Test that a backend checker which is executed and that require some specific
     * environment variables (e.g defined by the context) are found and validate the
     * healthy and operable state.
     */
    @Test
    public void givenValidSystemEnvironmentVariables_whenCheckConfigurationVariables_thenHealthyAndOperableStateConfirmed()
            throws Exception {
        // Simulate existent environment variables on a context where
        // operability checker could be used
        initMinimumRequiredEnvVariables();

        // Execute the checker process
        ExecutableBackendChecker checker = new ExecutableBackendChecker();
        checker.checkOperableState();
        // Valid that healthy state is delivered because none exception thrown
        assertTrue(checker.isOperableStateChecked());
    }

    /**
     * Test that all minimum environment variables needs by a backend are provided
     * by the checker as catalog of mandatory variables to verify.
     */
    @Test
    public void givenMinimumRequiredVariable_whenReadVariableToCheck_thenAllProvidedByChecker() {
        // Verify that all required variables have been found
        ExecutableBackendChecker checker = new ExecutableBackendChecker();
        Set<IReadableConfiguration> minimumEnvVariablesRequired = checker
                .requiredEnvironmentVariables();

        // verify provided quantity of minimum environment variables supported by
        // backend server
        assertNotNull(minimumEnvVariablesRequired);
        assertFalse(minimumEnvVariablesRequired.isEmpty());

        // Is there optional variable catalog not required
        Set<IReadableConfiguration> optionalVariables = checker.optionalEnvironmentVariables();

        // Check all environment variables required for write model access
        HashSet<IReadableConfiguration> toCheck = new HashSet<>(EnumSet.allOf(AppConfigurationVariable.class));
        toCheck.removeAll(optionalVariables);
        for (IReadableConfiguration aReq : toCheck) {
            // Verify if treated by checker
            assertTrue(minimumEnvVariablesRequired.contains(aReq), "Variable not verified by the checker!");
        }
    }

    /**
     * Test the start of backend Verticle rejected for cause of missing environment
     * variable (e.g http port).
     */
    @Test
    void givenUndefinedHttpPortEnvironmentVariable_whenHealthyStateChecked_thenMissingConfigurationException() {
        // None http port defined in environment variable

        Vertx vertx = Vertx.vertx();

        // Try backend module (Verticle deployment) start
        vertx.deployVerticle(AccessControlBackendServer.class.getName(),
                event -> assertTrue(event.failed(), "Start shall have been not executed for cause of undefined environment variable!"));
    }
}
