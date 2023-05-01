package org.cybnity.accesscontrol.authorization.domain.model;

import java.util.Collection;
import java.util.List;

import org.cybnity.accesscontrol.iam.domain.model.AuthorizationPolicy;
import org.cybnity.framework.immutable.Unmodifiable;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Represent a resource that access is controllable via authorization policy.
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_AC_3")
public abstract class ControlledResource implements IResource, Unmodifiable {

    /**
     * Original resource under control.
     */
    protected IResource resource;
    
    /**
     * Controls applied to this resource.
     */
    private Collection<AuthorizationPolicy> policies;

    /**
     * Default constructor.
     * 
     * @param resource Mandatory resource under control.
     * @param controls Minimun set of one policy ensuring the resource usage.
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     */
    public ControlledResource(IResource resource, Collection<AuthorizationPolicy> controls)
	    throws IllegalArgumentException {
	if (resource == null)
	    throw new IllegalArgumentException("Resource parameter is required!");
	if (controls == null || controls.isEmpty())
	    throw new IllegalArgumentException("One mininum control parameter is required!");
	this.resource = resource;
	this.policies = controls;
    }

    /**
     * Get the type of policies that constraint the accessibility and usage rules of
     * this resource.
     * 
     * @return Authorization controllers set.
     */
    public Collection<AuthorizationPolicy> controledBy() {
	// Return immutable collection of the controls
	return List.copyOf(this.policies);
    }

}
