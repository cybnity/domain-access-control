package org.cybnity.application.accesscontrol.ui.api;

import org.cybnity.application.accesscontrol.ui.api.routing.UISRecipientListUseCaseTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Suite of all technical and behavior unit tests regarding the implementation
 * components capabilities without need of platform.
 *
 * @author olivier
 */
@Suite
@SelectClasses({UISRecipientListUseCaseTest.class})
public class AllUseCaseTests {
}