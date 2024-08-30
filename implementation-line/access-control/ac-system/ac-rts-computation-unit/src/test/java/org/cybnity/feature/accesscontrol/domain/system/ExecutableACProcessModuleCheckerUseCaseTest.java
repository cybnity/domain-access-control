package org.cybnity.feature.accesscontrol.domain.system;

import io.vertx.core.Vertx;
import org.cybnity.framework.IReadableConfiguration;
import org.cybnity.framework.application.vertx.common.AppConfigurationVariable;
import org.cybnity.framework.immutable.utility.ExecutableComponentChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests regarding the utility class that check the healthy and operational
 * state of a process module runnable.
 *
 * @author olivier
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ExecutableACProcessModuleCheckerUseCaseTest extends CustomContextualizedTest {

    /**
     * Default constructor.
     */
    public ExecutableACProcessModuleCheckerUseCaseTest() {
        super(false, false, false, false, /* With snapshots management capability activated */ false);
    }

    /**
     * Test that a module checker which is executed and that require some specific
     * environment variables (e.g defined by the context) are found and validate the
     * healthy and operable state.
     */
    @Test
    public void givenValidSystemEnvironmentVariables_whenCheckConfigurationVariables_thenHealthyAndOperableStateConfirmed()
            throws Exception {
        // Execute the checker process
        ExecutableACProcessModuleChecker checker = new ExecutableACProcessModuleChecker();
        checker.checkOperableState();
        // Valid that healthy state is delivered because none exception thrown
        assertTrue(checker.isOperableStateChecked());
    }

    /**
     * Test that all minimum environment variables needs by the process module are provided
     * by the checker as catalog of mandatory variables to verify.
     */
    @Test
    public void givenMinimumRequiredVariable_whenReadVariableToCheck_thenAllProvidedByChecker() {
        // Verify that all required variables have been found
        ExecutableACProcessModuleChecker checker = new ExecutableACProcessModuleChecker();

        // Is there optional variable catalog not required
        Set<IReadableConfiguration> optionalVariables = checker.optionalEnvironmentVariables();

        // Check all application environment variables required for write/read models access
        HashSet<IReadableConfiguration> toCheck = new HashSet<>(EnumSet.allOf(AppConfigurationVariable.class));
        toCheck.removeAll(optionalVariables);

        // Verify the controlled variable of module
        validate(checker, toCheck);
    }

    /**
     * Execute check relative to a specific checker.
     *
     * @param checker To evaluate.
     */
    private void validate(ExecutableComponentChecker checker, HashSet<IReadableConfiguration> toCheck) {
        Set<IReadableConfiguration> minimumEnvVariablesRequired = checker
                .requiredEnvironmentVariables();

        // verify provided quantity of minimum environment variables supported
        assertNotNull(minimumEnvVariablesRequired);
        assertFalse(minimumEnvVariablesRequired.isEmpty());

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
        this.environmentVariables.remove(AppConfigurationVariable.ENDPOINT_HTTP_SERVER_PORT.getName());

        Vertx vertx = Vertx.vertx();

        // Try backend module (Verticle deployment) start
        vertx.deployVerticle(new AccessControlDomainProcessModule()).onComplete(handler -> {
            String result = handler.result();
            assertFalse(handler.succeeded(), "Start shall have been not executed for cause of undefined environment variable (result: " + result + ")!");
        });
    }
}
