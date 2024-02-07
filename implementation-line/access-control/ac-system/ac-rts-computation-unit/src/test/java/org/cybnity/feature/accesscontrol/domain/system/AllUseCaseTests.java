package org.cybnity.feature.accesscontrol.domain.system;

import org.cybnity.feature.accesscontrol.domain.system.service.TenantRegistrationUseCaseTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Suite of all technical and behavior unit tests regarding the implementation
 * components capabilities without need of platform.
 *
 * @author olivier
 */
@Suite
@SelectClasses({ExecutableACProcessModuleCheckerUseCaseTest.class, TenantRegistrationUseCaseTest.class})
public class AllUseCaseTests {
}