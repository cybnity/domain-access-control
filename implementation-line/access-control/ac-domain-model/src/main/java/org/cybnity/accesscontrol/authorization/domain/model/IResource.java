package org.cybnity.accesscontrol.authorization.domain.model;

import org.cybnity.framework.immutable.IReferenceable;
import org.cybnity.framework.immutable.IdentifiableFact;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Represents an object (e.g file, service, data) being protected, and that is
 * identifiable.
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_21")
public interface IResource extends IdentifiableFact, IReferenceable {

}
