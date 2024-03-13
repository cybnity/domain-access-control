package org.cybnity.accesscontrol.domain.service.api.model;

import java.util.*;

/**
 * Represents a collection of identified tenant versions states, known as its history.
 */
public class TenantTransactionsCollection {

    /**
     * Query parameter allowing search of tenant transactions in read-model projection.
     */
    public enum QueryParameter {
        TENANT_IDENTIFIER, TENANT_LABEL, TENANT_ACTIVITY_STATUS, TENANT_VERSION_DATE;
    }

    /**
     * Catalog of tenant transaction versions during the lifecycle of the identified Tenant.
     * Use a thread-safe view of the specified list allowing concurrent access to manipulate the history in a thread-safe fashion.
     * ArrayList is a best choice for frequent operation that is retrieval operation.
     * LinkedList is a best choice for frequent operation as insertion and deletion in the middle; but is a worst choice is the frequent operation is retrieval operation.
     */
    private final List<TenantTransaction> versionsHistory = Collections.synchronizedList(new ArrayList<>());

    /**
     * Identifier of the Tenant which is owner of transactions container.
     */
    private final String tenantIdentifier;

    /**
     * Default constructor.
     *
     * @param tenantId Mandatory technical unique identifier of the tenant.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public TenantTransactionsCollection(String tenantId) throws IllegalArgumentException {
        if (tenantId == null || tenantId.isEmpty())
            throw new IllegalArgumentException("Tenant identifier is required!");
        this.tenantIdentifier = tenantId;
    }

    /**
     * Search transaction in history that have equals definition value.
     *
     * @param label          Optional label naming search criteria.
     * @param activityStatus Optional tenant state search criteria.
     * @param versionedAt    Optional date of version search criteria.
     * @return Found transaction or null.
     */
    public Collection<TenantTransaction> findExistingEquals(String label, Boolean activityStatus, Date versionedAt) {
        Collection<TenantTransaction> found = new LinkedList<>();
        // Read history and compare search criteria to identify an equals version
        for (TenantTransaction t : versionsHistory) {
            boolean equalsLabel = (label == null), equalsStatus = (activityStatus == null), equalsVersionDate = (versionedAt == null);
            if (label != null) {
                // Compare search criteria
                equalsLabel = label.equals(t.label);
            }
            if (activityStatus != null) {
                // Compare search criteria
                equalsStatus = activityStatus.equals(t.activityStatus);
            }
            if (versionedAt != null) {
                // Compare search criteria
                equalsVersionDate = versionedAt.equals(t.versionedAt);
            }
            if (equalsLabel && equalsStatus && equalsVersionDate) {
                found.add(t);// Select as found equals version
            }
        }
        if (!found.isEmpty())
            return found;
        return null;
    }

    /**
     * Add tenant transaction into the set.
     *
     * @param label          Mandatory label naming the tenant.
     * @param activityStatus Optional tenant state. True when the activity status of the tenant is operable.
     * @param versionedAt    Optional date of tenant version creation. When null, the current date is selected as version of the created tenant transaction.
     * @throws IllegalArgumentException When any mandatory parameter is missing.
     */
    public void add(String label, Boolean activityStatus, Date versionedAt) throws IllegalArgumentException {
        // Create and add transaction item to transactions set
        versionsHistory.add(new TenantTransaction(activityStatus, label, versionedAt, this.tenantIdentifier));
    }

    /**
     * Get the list of items.
     *
     * @return An immutable list of transactions or empty list.
     */
    public List<TenantTransaction> versions() {
        return this.versionsHistory;
    }

    /**
     * Get technical unique identifier of the Tenant owner of these transactions.
     *
     * @return An identifier.
     */
    public String tenantIdentifier() {
        return this.tenantIdentifier;
    }

}
