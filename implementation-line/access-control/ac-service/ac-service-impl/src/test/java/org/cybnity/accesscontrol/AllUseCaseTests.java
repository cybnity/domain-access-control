package org.cybnity.accesscontrol;

import org.cybnity.accesscontrol.domain.infrastructure.impl.ACTransactionsRepositoryUseCaseTest;
import org.cybnity.accesscontrol.domain.service.impl.TenantRegistrationRejectionUseCaseTest;
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
@SelectClasses({TenantRegistrationUseCaseTest.class, TenantRegistrationRejectionUseCaseTest.class, ACTransactionsRepositoryUseCaseTest.class})
public class AllUseCaseTests {

}