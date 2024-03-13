package org.cybnity.accesscontrol.domain.service.api.model;

import org.cybnity.framework.domain.DataTransferObject;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * Read-Model VIEW of a Tenant aggregate version (e.g denormalized, limited to specific attributes), shareable out of the Access Control domain.
 * Represent a version of a Tenant entity, at a moment of its life.
 * Type of DTO implementation which can be used into read-model projection(s) stored/exposed via repository.
 */
public class TenantTransaction extends DataTransferObject {

    /**
     * Unique identifier of the tenant.
     */
    public final String identifiedBy;

    /**
     * Label naming the tenant.
     */
    public final String label;

    /**
     * Status of activation regarding the tenant.
     * True when the tenant is considered as active and in operable state.
     */
    public final Boolean activityStatus;

    /**
     * Date of the tenant versioning.
     */
    public Date versionedAt;

    /**
     * Default constructor of tenant version.
     *
     * @param activityStatus Optional state. True when the activity status of the tenant is operable.
     * @param label          Mandatory label naming the tenant.
     * @param versionedAt    Optional date of tenant version creation. When null, the current date is selected as version of the created tenant transaction.
     * @param tenantUID      Mandatory identifier of the tenant.
     * @throws IllegalArgumentException When any mandatory parameter is missing.
     */
    public TenantTransaction(Boolean activityStatus, String label, Date versionedAt, String tenantUID) throws IllegalArgumentException {
        if (tenantUID == null || tenantUID.isEmpty())
            throw new IllegalArgumentException("TenantUID parameter is required!");
        this.identifiedBy = tenantUID;
        this.activityStatus = activityStatus;
        if (label == null || label.isEmpty())
            throw new IllegalArgumentException("Tenant label is required and shall be defined!");
        this.label = label;
        // Initialize by default to now
        this.versionedAt = Objects.requireNonNullElseGet(versionedAt, () -> Date.from(OffsetDateTime.now().toInstant()));
    }

    @Override
    public boolean equals(Object obj) {
        boolean equalsObject = false;
        if (obj == this) {
            return true;
        } else {
            if (obj instanceof TenantTransaction) {
                TenantTransaction item = (TenantTransaction) obj;
                // Logical equality based on label
                if (item.label.equals(this.label)) {
                    equalsObject = true;
                }
            }

            return equalsObject;
        }
    }
}
