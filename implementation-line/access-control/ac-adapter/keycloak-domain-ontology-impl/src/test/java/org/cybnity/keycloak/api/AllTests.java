package org.cybnity.keycloak.api;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Suite of all technical and behavior tests.
 *
 * @author olivier
 */
@Suite
@SelectClasses({ResponseCodeIdentificationUseCaseTest.class})
public class AllTests {
}
