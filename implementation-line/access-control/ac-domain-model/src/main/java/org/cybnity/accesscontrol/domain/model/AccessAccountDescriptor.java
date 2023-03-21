package org.cybnity.accesscontrol.domain.model;

import java.io.Serializable;
import java.util.LinkedHashSet;

import org.cybnity.framework.IContext;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.IdentifierStringBased;
import org.cybnity.framework.domain.model.IAggregate;
import org.cybnity.framework.immutable.BaseConstants;
import org.cybnity.framework.immutable.Entity;
import org.cybnity.framework.immutable.EntityReference;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.framework.immutable.ImmutabilityException;
import org.cybnity.framework.immutable.utility.VersionConcreteStrategy;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Represent an allowed set of credentials (e.g cryptographic keys that enable
 * the subject to sign or encrypt data) to an owner (e.g organization's member),
 * according to a role (e.g user principal), with accorder privileges (e.g
 * regarding systems and/or capabilities) in the frame of a context (e.g
 * specific Tenant scope).
 * 
 * Domain aggregate object relative to a subject's usable account.
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_3")
public class AccessAccountDescriptor extends Entity implements IAggregate {

    /**
     * Owner entity (identifier) of this account.
     */
    private EntityReference owner;

    /**
     * Tenant entity (identifier) where this account can be used.
     */
    private EntityReference tenant;

    /**
     * Version of this class
     */
    private static final long serialVersionUID = new VersionConcreteStrategy()
	    .composeCanonicalVersionHash(AccessAccountDescriptor.class).hashCode();

    /**
     * Default constructor.
     * 
     * @param id             Mandatory identifier of this user account.
     * @param userIdentity   Mandatory user identity who is owner of this account.
     * @param tenantIdentity Optional identity of the tenant (e.g organization
     *                       subscription that this account is registered into)
     *                       where this account is usable.
     * @throws IllegalArgumentException When id parameter is null and does not
     *                                  include name and value.
     */
    protected AccessAccountDescriptor(Identifier id, EntityReference userIdentity, EntityReference tenantIdentity)
	    throws IllegalArgumentException {
	super(id);
	if (!BaseConstants.IDENTIFIER_ID.name().equals(id.name()))
	    throw new IllegalArgumentException(
		    "id parameter is not valid because identifier name shall be equals to only supported value ("
			    + BaseConstants.IDENTIFIER_ID.name() + ")!");
	if (userIdentity == null)
	    throw new IllegalArgumentException("userIdentity parameter is required!");
	// Save unmodifiable user identity which is owner of this account
	this.owner = userIdentity;
	// Save the tenant identity which is a scope of usage regarding this account
	// (e.g attached to the subcription of the tenant)
	this.tenant = tenantIdentity;
    }

    /**
     * Specific partial constructor of an identifiable account.
     * 
     * @param identifiers Set of mandatory identifiers of this entity, that contains
     *                    non-duplicable elements.
     * @throws IllegalArgumentException When identifiers parameter is null or each
     *                                  item does not include name and value.
     */
    private AccessAccountDescriptor(LinkedHashSet<Identifier> identifiers) throws IllegalArgumentException {
	super(identifiers);
    }

    @Override
    public Serializable immutable() throws ImmutabilityException {
	LinkedHashSet<Identifier> ids = new LinkedHashSet<>(this.identifiers());
	AccessAccountDescriptor account = new AccessAccountDescriptor(ids);
	account.createdAt = this.occurredAt();
	account.owner = this.owner();
	account.tenant = this.tenant();
	return account;
    }

    /**
     * Implement the generation of version hash regarding this class type according
     * to a concrete strategy utility service.
     */
    @Override
    public String versionHash() {
	return new VersionConcreteStrategy().composeCanonicalVersionHash(getClass());
    }

    @Override
    public Identifier identified() throws ImmutabilityException {
	StringBuffer combinedId = new StringBuffer();
	for (Identifier id : this.identifiers()) {
	    combinedId.append(id.value());
	}
	// Return combined identifier
	return new IdentifierStringBased(BaseConstants.IDENTIFIER_ID.name(), combinedId.toString());
    }

    /**
     * Get the reference of the owner of this account.
     * 
     * @return A reference immutable version. Null when problem of immutable
     *         reference generation.
     */
    public EntityReference owner() {
	try {
	    return (EntityReference) this.owner.immutable();
	} catch (Exception e) {
	    // Shall never arrive. Add implementation failure log
	}
	return null;
    }

    /**
     * Get the reference of the tenant where this account is usable.
     * 
     * @return A reference immutable version, or null when unknown.
     */
    public EntityReference tenant() {
	if (this.tenant != null) {
	    try {
		return (EntityReference) this.tenant.immutable();
	    } catch (Exception e) {
		// Shall never arrive. Add implementation failure log
	    }
	}
	return null;
    }

    @Override
    public void execute(Command change, IContext ctx) throws IllegalArgumentException {
	if (ctx == null)
	    throw new IllegalArgumentException("Context parameter is required!");

	throw new IllegalArgumentException(
		"Unsupported type of command by " + AccessAccountDescriptor.class.getName() + "!");
    }

}
