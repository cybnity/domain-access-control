package org.cybnity.accesscontrol.authorization.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * User claim.
 * A claim is a piece of information about a user (e.g; a "manager" as claim type, from "finance" department, and having permissions "Approve Invoice"), represented as a key–value pair.
 * Claims allow us to define what a user can do (like set of authorizations which define a specific function into an organization or a level of access into an application), not just who they are.
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public interface IClaim {

}
