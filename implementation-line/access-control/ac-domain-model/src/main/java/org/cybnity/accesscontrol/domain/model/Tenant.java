package org.cybnity.accesscontrol.domain.model;

import java.io.Serializable;
import java.util.LinkedHashSet;

import org.cybnity.framework.IContext;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.IdentifierStringBased;
import org.cybnity.framework.domain.model.IAggregate;
import org.cybnity.framework.immutable.BaseConstants;
import org.cybnity.framework.immutable.Entity;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.framework.immutable.ImmutabilityException;
import org.cybnity.framework.immutable.MutableProperty;
import org.cybnity.framework.immutable.utility.VersionConcreteStrategy;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Represent an organization suscription that allow to define a scope of
 * multi-tenant application regarding a named organization which facilitates the
 * users registrations through invitation.
 * 
 * This tenant resolve queries of application data (e.g segregation per
 * organization) and isolation of persistable contents (e.g database structure
 * including key of tenant for each stable; sharded database per tenant about
 * each Write/Read model) and/or resources allocation (e.g pool isolation model
 * per sharded database relative to the pool proportion user-base and resource
 * usage).
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_3")
public class Tenant extends Entity implements IAggregate {

    /**
     * Version of this class
     */
    private static final long serialVersionUID = new VersionConcreteStrategy().composeCanonicalVersionHash(Tenant.class)
	    .hashCode();

    /**
     * Logical name of this tenant (e.g business name of a company) that facilitate
     * to resolve queries.
     */
    private MutableProperty name;

    /**
     * Current status of activity regarding this tenant.
     */
    private ActivityState activityStatus;

    /**
     * Default constructor of a named tenant. By default, the tenant state is not
     * active.
     * 
     * @param id   Mandatory identifier of this tenant.
     * @param name Optional logical name (e.g organization name, owner or company
     *             specific official identifying information) of this tenant.
     * @throws IllegalArgumentException When any mandatory parameter is missing.
     *                                  When id parameter's name is not equals to
     *                                  BaseConstants.IDENTIFIER_ID.
     */
    public Tenant(Identifier id, MutableProperty name) throws IllegalArgumentException {
	this(id);
	this.name = name;
    }

    /**
     * Default constructor.
     * 
     * @param id   Mandatory identifier of this tenant.
     * @param name Optional logical name (e.g organization name, owner or company
     *             specific official identifying information) of this tenant.
     * @throws IllegalArgumentException When any mandatory parameter is missing.
     *                                  When id parameter's name is not equals to
     *                                  BaseConstants.IDENTIFIER_ID. When a problem
     *                                  of immutability is occurred.
     */
    public Tenant(Identifier id) throws IllegalArgumentException {
	super(id);
	if (!BaseConstants.IDENTIFIER_ID.name().equals(id.name()))
	    throw new IllegalArgumentException(
		    "id parameter is not valid because identifier name shall be equals to only supported value ("
			    + BaseConstants.IDENTIFIER_ID.name() + ")!");
    }

    /**
     * Specific partial constructor of an identifiable tenant.
     * 
     * @param identifiers Set of mandatory identifiers of this entity, that contains
     *                    non-duplicable elements.
     * @throws IllegalArgumentException When identifiers parameter is null or each
     *                                  item does not include name and value.
     */
    private Tenant(LinkedHashSet<Identifier> identifiers) throws IllegalArgumentException {
	super(identifiers);
    }

    /**
     * Get the current state of activity regarding this tenant.
     * 
     * @return A status.
     * @throws ImmutabilityException When problem of instantiation regarding the
     *                               immutable version of the current status.
     */
    public ActivityState status() throws ImmutabilityException {
	ActivityState current = null;
	if (this.activityStatus != null)
	    current = (ActivityState) this.activityStatus.immutable();
	return current;
    }

    /**
     * Change the current activity state as active. This method update the hsitory
     * of previous activity states and replace the current state with a new
     * immutable state version.
     * 
     * @return An immutable version of the new current state.
     * @throws ImmutabilityException When impossible assignation of this reference
     *                               as owner of an activity state change.
     */
    public MutableProperty activate() throws ImmutabilityException {
	ActivityState state = null;
	if (this.activityStatus == null) {
	    // Create initial status as active
	    state = new ActivityState(this.reference(), Boolean.TRUE);
	    this.activityStatus = state;
	} else {
	    // TODO: créer nouvelle version de status, alimenter et mettre à jour
	    // l'historique de la mutable property. Rédiger le TU
	}
	return (ActivityState) state.immutable();
    }

    /**
     * Change the current activity state as inactive. This method update the hsitory
     * of previous activity states and replace the current state with a new
     * immutable state version.
     * 
     * @return An immutable version of the new current state.
     * @throws ImmutabilityException When impossible assignation of this reference
     *                               as owner of an activity state change.
     */
    public MutableProperty deactivate() throws ImmutabilityException {
	ActivityState state = null;
	if (this.activityStatus == null) {
	    // Create initial status as inactive
	    state = new ActivityState(this.reference(), Boolean.FALSE);
	    this.activityStatus = state;
	} else {
	    // TODO: créer nouvelle version de status, alimenter et mettre à jour
	    // l'historique de la mutable property. Rédiger le TU
	}
	return (ActivityState) state.immutable();
    }

    /**
     * Define a logical name regarding this tenant when none previously defined.
     * When existent previous name about this tenant, this method make a change on
     * the previous defined name (changes history is saved) to defined the new name
     * as current.
     * 
     * @param tenantName A name.
     */
    public void setName(MutableProperty tenantName) {
	if (this.name != null) {
	    // Update the name with a new version

	    // TODO : créer un change avec historique alimentée
	} else {
	    // Initialize the first defined name of this tenant
	    this.name = tenantName;
	}
    }

    /**
     * Get the logical name of this tenant.
     * 
     * @return A name or null if unknown.
     * @throws ImmutabilityException When problem of immutable version
     *                               instantiation.
     */
    public MutableProperty name() throws ImmutabilityException {
	if (this.name != null)
	    return (MutableProperty) this.name.immutable();
	return null;
    }

    @Override
    public Serializable immutable() throws ImmutabilityException {
	LinkedHashSet<Identifier> ids = new LinkedHashSet<>(this.identifiers());
	Tenant tenant = new Tenant(ids);
	tenant.createdAt = this.occurredAt();
	tenant.name = this.name();
	tenant.activityStatus = this.status();
	return tenant;
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

    @Override
    public void execute(Command change, IContext ctx) throws IllegalArgumentException {
	if (ctx == null)
	    throw new IllegalArgumentException("Context parameter is required!");

	throw new IllegalArgumentException("Unsupported type of command by " + Tenant.class.getName() + "!");
    }

}
