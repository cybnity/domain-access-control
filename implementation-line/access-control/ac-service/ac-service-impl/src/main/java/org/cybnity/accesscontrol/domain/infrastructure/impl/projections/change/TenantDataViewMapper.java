package org.cybnity.accesscontrol.domain.infrastructure.impl.projections.change;

import org.cybnity.accesscontrol.domain.service.api.model.TenantDataView;
import org.cybnity.framework.domain.AbstractDTOMapper;
import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.event.ConcreteDomainChangeEvent;
import org.cybnity.framework.domain.event.EventSpecification;
import org.cybnity.framework.domain.infrastructure.IDomainStore;
import org.cybnity.framework.domain.model.ActivityState;
import org.cybnity.framework.domain.model.CommonChildFactImpl;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.domain.model.TenantDescriptor;
import org.cybnity.framework.immutable.EntityReference;
import org.cybnity.framework.immutable.Identifier;

import java.util.Date;

/**
 * DTO mapping implementation class ensuring the preparation of a data view version relative to a Tenant.
 */
public class TenantDataViewMapper extends AbstractDTOMapper<TenantDataView> {
    /**
     * Store of tenant versions.
     */
    private final IDomainStore<Tenant> tenantsWriteModelStore;

    /**
     * Default constructor.
     *
     * @param tenantsWriteModelStore Mandatory store of tenant as write-model.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public TenantDataViewMapper(IDomainStore<Tenant> tenantsWriteModelStore) throws IllegalArgumentException {
        if (tenantsWriteModelStore == null)
            throw new IllegalArgumentException("tenantsWriteModelStore parameter is required!");
        this.tenantsWriteModelStore = tenantsWriteModelStore;
    }

    /**
     * Conversion method that ensure transformation of a collection of attributes relative to a DomainEvent, into a simplified version of data view (queryable and immutable) supported by a read-model projection.
     *
     * @param source Mandatory domain event object type which is provider of attributes to read about a Tenant description.
     * @return An instantiated version of data view including only the data that make sens for a projection.
     * @throws IllegalArgumentException      When mandatory parameter is not defined. When event.typ().value() of source event is not equals to DomainEventType.TENANT_CREATED.name() normally representing a tenant creation confirmed by a write-model change operation.
     * @throws UnsupportedOperationException When impossible to read attributes that are required for data view instance creation.
     */
    @Override
    public TenantDataView convertTo(Object source) throws IllegalArgumentException, UnsupportedOperationException {
        if (source == null) throw new IllegalArgumentException("Source parameter is required!");
        // Identify and check that is a supported event type
        try {
            // Check to cast into the expected type supported for read of attributes

            // Normally an event promoted by a store relative to a tenant created or changed is a ConcreteDomainChangeEvent type
            // So check if good event type (provider of complementary information that make sens for the data view version to produce)
            if (!ConcreteDomainChangeEvent.class.isAssignableFrom(source.getClass()))
                throw new IllegalArgumentException("Invalid source event nature that shall be a ConcreteDomainChangeEvent event type!");
            ConcreteDomainChangeEvent event = (ConcreteDomainChangeEvent) source;

            // --- Read source data values to be reused into the data view to generate ---
            // Read the domain object's identifier that have been changed
            EntityReference changedWriteModelObjectRef = event.changedModelElementReference();
            Identifier sourceDomainObjId = event.changeSourceIdentifier();
            // Only creation date, tenant id and origin creation event request predecessor's id can be known from received event

            // Select between multiple potential means for changes domain object identification
            if (changedWriteModelObjectRef != null || sourceDomainObjId != null) {
                Identifier detectedDomainObjectChangedId = (changedWriteModelObjectRef != null) ? changedWriteModelObjectRef.getEntity().identified() : sourceDomainObjId;

                // Load initial values of the tenant projected version that is KNOWN BY THE WRITE-MODEL
                Tenant fullRehydratedTenantVersion = this.tenantsWriteModelStore.findEventFrom(detectedDomainObjectChangedId);
                if (fullRehydratedTenantVersion != null) {
                    // READ CURRENT WRITE-MODEL VERSION AND GENERATE ITS VIEW VERSION

                    // --- MANDATORY INFORMATION RELATIVE TO A READ TENANT ---
                    // Identify the Tenant's unique identifier
                    Identifier tenantId = fullRehydratedTenantVersion.identified();
                    if (event.changeSourceIdentifier() != null && !event.changeSourceIdentifier().equals(tenantId)) {
                        tenantId = event.changeSourceIdentifier();
                    } // Origin domain model object created
                    String tenantIdentifier = tenantId.value().toString(); // Read identifier value

                    // Identify date of creation (equals to source domain object occurrence date)
                    // Determine the read-model projection data (primarily based on change notification date, else based on last version rehydrated)
                    Date occurredAt = Date.from(fullRehydratedTenantVersion.occurredAt().toInstant());
                    // --------------

                    // --- OPTIONAL INFORMATION RELATIVE TO A READ TENANT ---
                    // Identify creation event's source identifier (generating a data-view creation) or explicit known domain tenant's commit version
                    String committedVersion = fullRehydratedTenantVersion.getCommitVersion();
                    Attribute commitVersion = EventSpecification.findSpecificationByName(CommonChildFactImpl.Attribute.COMMIT_VERSION.name(), event.specification());
                    if (commitVersion != null && commitVersion.value() != null && !commitVersion.value().isEmpty()) {
                        committedVersion = commitVersion.value();
                    }
                    if (committedVersion == null || committedVersion.isEmpty())
                        committedVersion = event.identified().value().toString(); // defined commit version as equals to the event identifier

                    // Identify the activity status
                    ActivityState currentState = fullRehydratedTenantVersion.status();
                    Boolean activityStatus = (currentState != null) ? currentState.isActive() : null;
                    Attribute activityStateAttr = EventSpecification.findSpecificationByName(Tenant.Attribute.ACTIVITY_STATUS.name(), event.specification());
                    if (activityStateAttr != null && activityStateAttr.value() != null && !activityStateAttr.value().isEmpty()) {
                        // State have been changed
                        activityStatus = Boolean.valueOf(activityStateAttr.value());
                    }

                    // Identify the unique logical label naming the Tenant (or define from tenant identifier as unique view label)
                    // Tenant label can be optionally defined during the Tenant creation (but is not a mandatory specification)
                    // As logical label is required for creation of a data view version (equals to vertex name),
                    // the INITIAL NAME of the data view version to create SHALL BE EQUALS TO:
                    // - LOGICAL NAME OF TENANT WHEN DEFINED
                    // - or TECHNICAL IDENTIFIER OF TENANT WHEN LOGICAL NAME OF TENANT UNKNOWN
                    TenantDescriptor labelDescriptor = fullRehydratedTenantVersion.label();
                    String label = (labelDescriptor != null) ? labelDescriptor.getLabel() : null;
                    // When initial creation event relative to a tenant, possible not already stored label change that will arrive in a second change event
                    // Check if possible identification of original known label from the creation event
                    Attribute labelAttr = EventSpecification.findSpecificationByName(Tenant.Attribute.LABEL.name(), event.specification());
                    if (labelAttr != null && labelAttr.value() != null && !labelAttr.value().isEmpty())
                        label = labelAttr.value(); // Identify predictive label (that will be notified to repository over future change event) that allow generation of first data view node

                    if (label == null || label.isEmpty())
                        label = tenantIdentifier; // defined technical id as default label
                    // --------------

                    // Try instantiation which is responsible for mandatory information required for creation
                    return new TenantDataView(activityStatus, label, occurredAt, tenantIdentifier,/* The date of occurrence is considered as the date of Tenant creation */ occurredAt, committedVersion);
                } else {
                    throw new IllegalArgumentException("The rehydrated tenant version of changed origin domain object is not available for transformation into Tenant data view queryable projection!");
                }
            }
            throw new IllegalArgumentException("Unidentifiable origin domain object to map for data view!");
        } catch (Exception cce) {
            throw new UnsupportedOperationException("Invalid source parameter and read value!", cce);
        }
    }
}
