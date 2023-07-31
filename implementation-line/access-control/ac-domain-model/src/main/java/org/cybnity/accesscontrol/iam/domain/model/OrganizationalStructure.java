package org.cybnity.accesscontrol.iam.domain.model;

import java.io.Serializable;
import java.util.LinkedHashSet;

import org.cybnity.framework.domain.model.SocialEntity;
import org.cybnity.framework.immutable.Entity;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.framework.immutable.ImmutabilityException;
import org.cybnity.framework.immutable.utility.VersionConcreteStrategy;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Represent an organizational structure (e.g company, association, group of
 * companies, institution) who can have interactions with systems.
 * 
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_3")
public class OrganizationalStructure extends SocialEntity {

    /**
     * Version of this class type.
     */
    private static final long serialVersionUID = new VersionConcreteStrategy()
	    .composeCanonicalVersionHash(OrganizationalStructure.class).hashCode();

    /**
     * Default constructor.
     * 
     * @param predecessor Mandatory parent of this child entity.
     * @param id          Unique and optional identifier of this instance.
     * @throws IllegalArgumentException When predecessor mandatory parameter is not
     *                                  defined or without defined identifier.
     */
    public OrganizationalStructure(Entity predecessor, Identifier id) throws IllegalArgumentException {
	super(predecessor, id);
    }

    /**
     * Default constructor.
     * 
     * @param predecessor Mandatory parent of this child entity.
     * @param identifiers Set of optional base identifiers of this instance, that
     *                    contains non-duplicable elements.
     * @throws IllegalArgumentException When identifiers parameter is null or each
     *                                  item does not include name and value.
     */
    public OrganizationalStructure(Entity predecessor, LinkedHashSet<Identifier> identifiers)
	    throws IllegalArgumentException {
	super(predecessor, identifiers);
    }

    @Override
    public Serializable immutable() throws ImmutabilityException {
	LinkedHashSet<Identifier> ids = new LinkedHashSet<>(this.identifiers());
	OrganizationalStructure organization = new OrganizationalStructure(parent(), ids);
	organization.createdAt = this.occurredAt();
	return organization;
    }

}
