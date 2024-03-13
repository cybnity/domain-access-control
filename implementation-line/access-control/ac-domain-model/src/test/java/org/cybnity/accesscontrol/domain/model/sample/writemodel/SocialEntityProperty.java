package org.cybnity.accesscontrol.domain.model.sample.writemodel;

import org.cybnity.framework.immutable.Entity;
import org.cybnity.framework.immutable.HistoryState;
import org.cybnity.framework.immutable.ImmutabilityException;
import org.cybnity.framework.immutable.MutableProperty;
import org.cybnity.framework.immutable.utility.VersionConcreteStrategy;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Modifiable definition regarding a social entity that can be changed
 * (e.g company name changed during its life), and which need to historized in an
 * immutable way the history of changes (version of this information).
 *
 * @author olivier
 */
public class SocialEntityProperty extends MutableProperty {

    private static final long serialVersionUID = 1L;
    private OffsetDateTime versionedAt;

    /**
     * Keys set regarding the multiple attribute defining this complex
     * social entity, and that each change need to be versioned/treated as a single
     * atomic fact.
     */
    public enum PropertyAttributeKey {
        Name, LocationCity, LocationCountry;
    }

    public SocialEntityProperty(Entity propertyOwner, HashMap<String, Object> propertyCurrentValue, HistoryState status)
            throws IllegalArgumentException {
        super(propertyOwner, propertyCurrentValue, status);
        this.versionedAt = OffsetDateTime.now();
    }

    public SocialEntityProperty(Entity propertyOwner, HashMap<String, Object> propertyCurrentValue, HistoryState status,
                                SocialEntityProperty... prior) throws IllegalArgumentException {
        super(propertyOwner, propertyCurrentValue, status, prior);
        this.versionedAt = OffsetDateTime.now();
    }

    /**
     * Get the name of the organization.
     *
     * @return A label or null.
     */
    public String getOrganizationName() {
        return (String) this.currentValue().getOrDefault(PropertyAttributeKey.Name.name(), null);
    }

    @Override
    public OffsetDateTime occurredAt() {
        return this.versionedAt;
    }

    @Override
    public Serializable immutable() throws ImmutabilityException {
        SocialEntityProperty copy = new SocialEntityProperty(this.owner(),
                new HashMap<String, Object>(this.currentValue()), this.historyStatus());
        // Complete with additional attributes of this complex property
        copy.versionedAt = this.versionedAt;
        copy.changedAt = this.occurredAt();
        copy.updateChangesHistory(this.changesHistory());
        return copy;
    }

    /**
     * Get the current value of this complex property.
     *
     * @return A set of valued attributes.
     */
    public Map<String, Object> currentValue() {
        return Collections.unmodifiableMap(this.value);
    }

    /**
     * Who is the owner of this property
     *
     * @return The owner
     * @throws ImmutabilityException If impossible creation of immutable version of
     *                               instance
     */
    public Entity owner() throws ImmutabilityException {
        return (Entity) this.owner.immutable();
    }

    /**
     * Implement the generation of version hash regarding this class type according
     * to a concrete strategy utility service.
     */
    @Override
    public String versionHash() {
        return new VersionConcreteStrategy().composeCanonicalVersionHash(getClass());
    }
}
