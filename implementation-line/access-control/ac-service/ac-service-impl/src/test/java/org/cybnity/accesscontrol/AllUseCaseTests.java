package org.cybnity.accesscontrol;

import org.cybnity.accesscontrol.domain.infrastructure.impl.ACTransactionsRepositoryUseCaseIntegrationTest;
import org.cybnity.accesscontrol.domain.service.impl.TenantRegistrationRejectionUseCaseIntegrationTest;
import org.cybnity.accesscontrol.domain.service.impl.TenantRegistrationUseCaseIntegrationTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Suite of all technical and behavior unit tests regarding the implementation
 * components capabilities without need of platform.
 *
 * @author olivier
 */
@Suite
@SelectClasses({TenantRegistrationUseCaseIntegrationTest.class, TenantRegistrationRejectionUseCaseIntegrationTest.class, ACTransactionsRepositoryUseCaseIntegrationTest.class})
public class AllUseCaseTests {

}