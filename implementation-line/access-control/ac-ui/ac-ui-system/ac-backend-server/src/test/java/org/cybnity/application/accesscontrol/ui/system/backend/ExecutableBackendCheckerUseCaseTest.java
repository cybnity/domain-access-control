package org.cybnity.application.accesscontrol.ui.system.backend;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.cybnity.framework.IReadableConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests regarding the utility class that check the healthy and operational
 * state of a backend runnable.
 *
 * @author olivier
 */
@ExtendWith({VertxExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ExecutableBackendCheckerUseCaseTest extends BackendCustomContextualizedTest {

    /**
     * Default constructor.
     */
    public ExecutableBackendCheckerUseCaseTest() {
        super(false, false, false, false, /* With snapshots management capability activated */ false);
    }

    /**
     * Test that a backend checker which is executed and that require some specific
     * environment variables (e.g defined by the context) are found and validate the
     * healthy and operable state.
     */
    @Test
    public void givenValidSystemEnvironmentVariables_whenCheckConfigurationVariables_thenHealthyAndOperableStateConfirmed()
            throws Exception {
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
        this.environmentVariables.remove(AppConfigurationVariable.REACTIVE_BACKEND_ENDPOINT_HTTP_SERVER_PORT.getName());

        Vertx vertx = Vertx.vertx();

        // Try backend module (Verticle deployment) start
        vertx.deployVerticle(new AccessControlReactiveMessagingGateway())
                .onSuccess(res -> {
                    fail("Start shall have been not executed for cause of undefined environment variable!");
                }).onFailure(e -> {
                    assertNotNull(e, "Start Shall have been rejected");
                });
    }
}
