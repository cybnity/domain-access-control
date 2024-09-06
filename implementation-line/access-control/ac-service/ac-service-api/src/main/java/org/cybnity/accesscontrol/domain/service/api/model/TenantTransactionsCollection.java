package org.cybnity.accesscontrol.domain.service.api.model;

import org.cybnity.framework.domain.DataTransferObject;
import org.cybnity.framework.domain.SerializationFormat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a collection of identified tenant versions states, known as its history.
 */
public class TenantTransactionsCollection extends DataTransferObject {

    /**
     * Catalog of tenant transaction versions during the lifecycle of the identified Tenant.
     * Use a thread-safe view of the specified list allowing concurrent access to manipulate the history in a thread-safe fashion.
     * ArrayList is the best choice for frequent operation that is retrieval operation.
     * LinkedList is the best choice for frequent operation as insertion and deletion in the middle; but is a worst choice is the frequent operation is retrieval operation.
     */
    private final List<TenantDataView> versionsHistory = Collections.synchronizedList(new ArrayList<>());

    /**
     * Identifier of the Tenant which is owner of transactions container.
     */
    private final String tenantIdentifier;

    /**
     * Logger for technical traceability.
     */
    private static final Logger logger = Logger.getLogger(TenantTransactionsCollection.class.getSimpleName());

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
     * This history is as container of tenant transactions, and is dedicated to only defined a set of transactions.
     *
     * @param o To compare.
     * @return True when equals collection items and tenant identifier.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenantTransactionsCollection)) return false;
        if (!super.equals(o)) return false;
        TenantTransactionsCollection that = (TenantTransactionsCollection) o;
        return Objects.equals(versionsHistory, that.versionsHistory) && Objects.equals(tenantIdentifier, that.tenantIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionsHistory, tenantIdentifier);
    }

    /**
     * Search transaction in history that have equals definition value.
     *
     * @param label          Optional label naming search criteria.
     * @param activityStatus Optional tenant state search criteria.
     * @param versionedAt    Optional date of version search criteria.
     * @return Found transaction or null.
     */
    public Collection<TenantDataView> findExistingEquals(String label, Boolean activityStatus, Date versionedAt) {
        Collection<TenantDataView> found = new LinkedList<>();
        // Read history and compare search criteria to identify an equals version
        for (TenantDataView t : versionsHistory) {
            boolean equalsLabel = (label == null), equalsStatus = (activityStatus == null), equalsVersionDate = (versionedAt == null);
            if (label != null) {
                // Compare search criteria
                equalsLabel = label.equals(t.valueOfProperty(TenantDataView.PropertyAttributeKey.LABEL));
            }
            if (activityStatus != null) {
                // Compare search criteria
                String currentStatus = t.valueOfProperty(TenantDataView.PropertyAttributeKey.ACTIVITY_STATUS);
                if (currentStatus != null) {
                    equalsStatus = Boolean.valueOf(currentStatus).equals(activityStatus);
                }
            }
            if (versionedAt != null) {
                DateFormat formatter = new SimpleDateFormat(SerializationFormat.DATE_FORMAT_PATTERN);
                try {
                    // Compare search criteria
                    equalsVersionDate = versionedAt.equals(formatter.parse(t.valueOfProperty(TenantDataView.PropertyAttributeKey.LAST_UPDATED_AT)));
                } catch (ParseException pe) {
                    logger.log(Level.SEVERE, "Invalid formatted value of LAST_UPDATED_AT property detected into data view!", pe);
                }
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
     * @param createdAt      Optional date of original tenant (write model) creation. When null, the date of creation of this data view is automatically set to now.
     * @param commitVersion  Optional identifier of the commit version relative to the tenant change transaction.
     * @throws IllegalArgumentException When any mandatory parameter is missing.
     */
    public void add(String label, Boolean activityStatus, Date versionedAt, Date createdAt, String commitVersion) throws IllegalArgumentException {
        // Create and add transaction item to transactions set
        versionsHistory.add(new TenantDataView(activityStatus, label, versionedAt, this.tenantIdentifier, createdAt, commitVersion));
    }

    /**
     * Add tenant transaction into the set.
     *
     * @param view Mandatory view to add.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public void add(TenantDataView view) throws IllegalArgumentException {
        if (view == null) throw new IllegalArgumentException("View parameter is required!");
        // Add transaction item to set
        versionsHistory.add(view);
    }

    /**
     * Get the list of items.
     *
     * @return An immutable list of transactions or empty list.
     */
    public List<TenantDataView> versions() {
        return Collections.unmodifiableList(this.versionsHistory);
    }

    /**
     * Get technical unique identifier of the Tenant owner of these transactions.
     *
     * @return An identification value (immutable version).
     */
    public String tenantIdentifier() {
        return this.tenantIdentifier;
    }

}
