package org.cybnity.accesscontrol.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cybnity.framework.domain.ValueObject;
import org.cybnity.framework.immutable.IHistoricalFact;
import org.cybnity.framework.immutable.IVersionable;
import org.cybnity.framework.immutable.ImmutabilityException;
import org.cybnity.framework.immutable.utility.VersionConcreteStrategy;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;

/**
 * Data Transfer Object representing a Tenant temp version.
 * Usable for transfer between internal and external domain via communication systems.
 * Can be used during a translation betwwen a 3rd-party ontology (e.g; Keycloak ontology Realm object type) with the access control domain.
 * Unique timely versioned status of a Tenant, this is a structured object reserved for data transport.
 */
public class TenantDTO extends ValueObject<Serializable> implements IVersionable, IHistoricalFact {

    /**
     * Version of this class type.
     */
    @JsonIgnore
    private static final long serialVersionUID = new VersionConcreteStrategy().composeCanonicalVersionHash(TenantDTO.class).hashCode();

    /**
     * Date of the status regarding this tenant information as a timed version.
     * It's not a date of DTO instantiation, but represent the date of DTO contents (e.g; that have been read from the domain which is owner of the information lifecycle).
     */
    private OffsetDateTime statusAt;

    /**
     * Date when this DTO instance have been created.
     */
    private OffsetDateTime occurredOn;

    /**
     * Unique name of the tenant.
     */
    private String label;

    /**
     * The current status of this tenant.
     */
    private Status currentStatus;

    /**
     * Default constructor of empty transport object.
     */
    public TenantDTO() {
        super();
        // Create immutable time of this dto creation
        this.occurredOn = OffsetDateTime.now();
    }

    /**
     * Constructor of tenant that is named by a label.
     *
     * @param label         Name of the tenant.
     * @param currentStatus Known statue of the tenant.
     */
    public TenantDTO(String label, Status currentStatus) {
        super();
        // Create immutable time of this dto creation
        this.occurredOn = OffsetDateTime.now();
        this.setLabel(label);
        this.currentStatus = currentStatus;
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
    public Serializable immutable() throws ImmutabilityException {
        return new TenantDTO(this.label, this.currentStatus);
    }

    /**
     * This method get all values that are functionally equal also produce equal
     * hash code value. This method is called by default hashCode() method of this
     * ValueObject instance and shall provide the list of values contributing to
     * define the unicity of this instance (e.g also used for valueEquality()
     * comparison).
     *
     * @return The unique functional values used to identify uniquely this instance.
     * Or empty array.
     */
    @Override
    public String[] valueHashCodeContributors() {
        try {
            // Prepare contributors based on existing attributes
            ArrayList<String> contributors = new ArrayList<>();
            if (this.label != null && !this.label.isEmpty())
                contributors.add(this.label); // name of the tenant
            contributors.add(this.occurredAt().toString()); // Specific time when this DTO version (a dto instance created at a different moment can be considered as different technically for hashcode)
            if (this.currentStatus != null) contributors.add(this.currentStatus.toString());
            return contributors.toArray(new String[0]);
        } catch (Exception ie) {
            // In case of null pointer exception
            return new String[]{};
        }
    }

    /**
     * Get the label naming the tenant.
     *
     * @return A name or null.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Define the name of the tenant.
     *
     * @param label A name.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Get the current status of the tenant.
     *
     * @return A status or null when unknown.
     */
    public Status getCurrentStatus() {
        return currentStatus;
    }

    /**
     * Define the current state of the tenant.
     *
     * @param currentStatus A status.
     */
    public void setCurrentStatus(Status currentStatus) {
        this.currentStatus = currentStatus;
    }

    /**
     * Get the time about the version of the tenant.
     *
     * @return A date equals to the moment when the DTO version of the tenant have been created.
     */
    public OffsetDateTime getOccurredOn() {
        return occurredAt();
    }

    /**
     * Define the date when the tenant represented by this DTO is considered as a timed version.
     *
     * @param occurredOn A date.
     */
    public void setOccurredOn(OffsetDateTime occurredOn) {
        this.occurredOn = occurredOn;
    }

    /**
     * Default implementation of dto time when it was created.
     */
    @Override
    public OffsetDateTime occurredAt() {
        // Return copy of the fact time
        return OffsetDateTime.parse(this.occurredOn.toString());
    }

    /**
     * State relative to a Tenant lifecycle.
     */
    public enum Status {
        /**
         * Active state of a tenant which is operational and managed.
         */
        TENANT_ENABLED,

        /**
         * Inactive state of a tenant which is existing but that is not operational (e.g; temporary disabled for maintenance operations and/or security concern).
         */
        TENANT_DISABLED;
    }

}
