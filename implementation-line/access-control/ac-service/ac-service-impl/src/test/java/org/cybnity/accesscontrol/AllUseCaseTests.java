package org.cybnity.accesscontrol;

import org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.projections.TenantLabelOptimizedProjectionUseCaseTest;
import org.cybnity.accesscontrol.domain.service.impl.TenantRegistrationUseCaseTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Suite of all technical and behavior unit tests regarding the implementation
 * components capabilities without need of platform.
 *
 * @author olivier
 */
@Suite
@SelectClasses({TenantLabelOptimizedProjectionUseCaseTest.class, TenantRegistrationUseCaseTest.class})
public class AllUseCaseTests {

}