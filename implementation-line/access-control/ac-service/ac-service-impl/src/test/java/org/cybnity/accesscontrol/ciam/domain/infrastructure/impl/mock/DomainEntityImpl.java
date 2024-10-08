package org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.mock;

import org.cybnity.framework.domain.IdentifierStringBased;
import org.cybnity.framework.immutable.Entity;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.framework.immutable.ImmutabilityException;
import org.cybnity.framework.immutable.utility.VersionConcreteStrategy;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;

/**
 * Basic and common domain entity implementation object.
 * 
 * A domain entity IS NOT MODIFIABLE and is equals to an identifiable fact.
 * 
 * A domain entity DOES NOT CONTAIN MUTABLE properties.
 * 
 * @author olivier
 *
 */
public class DomainEntityImpl extends Entity {

	/**
	 * Version of this class type.
	 */
	private static final long serialVersionUID = new VersionConcreteStrategy()
			.composeCanonicalVersionHash(DomainEntityImpl.class).hashCode();

	/**
	 * Default constructor.
	 * 
	 * @param id Unique and mandatory identifier of this entity.
	 * @throws IllegalArgumentException When id parameter is null and does not
	 *                                  include name and value.
	 */
	public DomainEntityImpl(Identifier id) throws IllegalArgumentException {
		super(id);
	}

	/**
	 * Default constructor.
	 * 
	 * @param identifiers Set of mandatory identifiers of this entity, that contains
	 *                    non-duplicated elements.
	 * @throws IllegalArgumentException When identifiers parameter is null or each
	 *                                  item does not include name and value.
	 */
	public DomainEntityImpl(LinkedHashSet<Identifier> identifiers) throws IllegalArgumentException {
		super(identifiers);
	}

	@Override
	public Identifier identified() {
		return IdentifierStringBased.build(this.identifiers());
	}

	@Override
	public Serializable immutable() throws ImmutabilityException {
		LinkedHashSet<Identifier> ids = new LinkedHashSet<>(this.identifiers());
		return new DomainEntityImpl(ids);
	}

	/**
	 * Implement the generation of version hash regarding this class type according
	 * to a concrete strategy utility service.
	 */
	@Override
	public String versionHash() {
		return new VersionConcreteStrategy().composeCanonicalVersionHash(getClass());
	}

	public void setCreatedAt(OffsetDateTime time) {
		if (time != null)
			this.createdAt = time;
	}
}
