package org.cybnity.application.accesscontrol.domain.system.gateway;

import org.cybnity.application.accesscontrol.domain.system.gateway.service.APISupportedCapabilitySelectionFilterUseCaseTest;
import org.cybnity.application.accesscontrol.domain.system.gateway.service.DynamicRecipientsSyncUseCaseTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Suite of all technical and behavior unit tests regarding the implementation
 * components capabilities without need of platform.
 *
 * @author olivier
 */
@Suite
@SelectClasses({ExecutableIOGatewayCheckerUseCaseTest.class, APISupportedCapabilitySelectionFilterUseCaseTest.class, DynamicRecipientsSyncUseCaseTest.class})
public class AllUseCaseTests {
}