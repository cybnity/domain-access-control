package org.cybnity.application.accesscontrol.ui.system.backend;

import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.junit5.VertxExtension;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

/**
 * Test of behavior regarding the Vert.x backend server.
 *
 * @author olivier
 */
@ExtendWith({VertxExtension.class, SystemStubsExtension.class})
public class ACBackendServerTest {

    @SystemStub
    private static EnvironmentVariables environmentVariables;

}
