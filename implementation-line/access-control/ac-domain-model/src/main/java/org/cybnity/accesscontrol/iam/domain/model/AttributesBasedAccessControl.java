package org.cybnity.accesscontrol.iam.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

import java.util.Collection;
import java.util.List;

/**
 * Represent a policy strategy type as policy of policies that depends of context.
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_AC_3")
public class AttributesBasedAccessControl extends AuthorizationPolicy {

    private final Collection<SubjectAttribute> subjectDescription;
    private final Collection<IActionAttribute> actionableActions;
    private final Collection<IEnvironmentAttribute> environmentDescription;

    /**
     * Default constructor.
     * 
     * @param subjectDescription     Mandatory minimum one subject description item.
     * @param actionableActions      Mandatory minimum one realizable action.
     * @param environmentDescription Mandatory minimum one context description.
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     */
    public AttributesBasedAccessControl(Collection<SubjectAttribute> subjectDescription,
                                        Collection<IActionAttribute> actionableActions, Collection<IEnvironmentAttribute> environmentDescription)
	    throws IllegalArgumentException {
	if (subjectDescription == null || subjectDescription.isEmpty())
	    throw new IllegalArgumentException("Subject description is required including minimum of one item!");
	if (actionableActions == null || actionableActions.isEmpty())
	    throw new IllegalArgumentException("Actionable action is required including minimum of one item!");
	if (environmentDescription == null || environmentDescription.isEmpty())
	    throw new IllegalArgumentException("Environment description is required including minimum of one item!");
	this.subjectDescription = subjectDescription;
	this.actionableActions = actionableActions;
	this.environmentDescription = environmentDescription;
    }

    /**
     * Get description criteria regarding the authorizable subject type.
     * 
     * @return An immutable version of the subject description attributes.
     */
    public Collection<SubjectAttribute> subjectDescription() {
	return List.copyOf(this.subjectDescription);
    }

    /**
     * Get description criteria regarding the authorizable actions.
     * 
     * @return An immutable version of the action types.
     */
    public Collection<IActionAttribute> actionableActions() {
	return List.copyOf(this.actionableActions);
    }

    /**
     * Get description criteria regarding the authorizable environment type.
     * 
     * @return An immutable version of the environment description attributes.
     */
    public Collection<IEnvironmentAttribute> environmentDescription() {
	return List.copyOf(this.environmentDescription);
    }
}
