package org.cybnity.accesscontrol.domain.model;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Suite of all technical and behavior tests regarding the domain components
 * capabilities.
 * 
 * @author olivier
 *
 */
@Suite
@SelectClasses({ ActivityStateUseCaseTest.class, AccountUseCaseTest.class })
public class AllTests {
}