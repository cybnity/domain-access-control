package org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.projections;

import org.cybnity.accesscontrol.domain.service.api.ciam.ITenantTransactionProjection;
import org.cybnity.accesscontrol.domain.service.api.model.TenantTransaction;
import org.cybnity.accesscontrol.domain.service.api.model.TenantTransactionsCollection;
import org.cybnity.application.accesscontrol.ui.api.event.AttributeName;
import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.DomainEvent;
import org.cybnity.framework.domain.ISessionContext;
import org.cybnity.framework.domain.event.ConcreteDomainChangeEvent;
import org.cybnity.framework.domain.event.DomainEventType;
import org.cybnity.framework.domain.event.EventSpecification;
import org.cybnity.framework.domain.infrastructure.IDomainRepository;
import org.cybnity.framework.domain.infrastructure.IDomainStore;
import org.cybnity.framework.domain.model.ActivityState;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.EntityReference;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.framework.immutable.ImmutabilityException;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represent a provider of read-model projection type that is optimized for query and read of denormalized versions of Tenant domain object.
 * This projection is optimized for search of Tenants from their labels.
 * A Tenant label is defined as unique into the tenants repository, and is ordered into an ascending approach.
 */
public class TenantLabelOptimizedProjectionImpl implements ITenantTransactionProjection {

    /**
     * Persistence layer of read-model managing the projection of tenants optimized for search via their labels.
     */
    private final IDomainRepository<TenantTransactionsCollection> persistenceLayer;

    /**
     * Technical logger.
     */
    private final Logger logger = Logger.getLogger(TenantLabelOptimizedProjectionImpl.class.getName());

    /**
     * Store of tenant versions.
     */
    private final IDomainStore<Tenant> tenantsWriteModelStore;

    /**
     * Default constructor.
     *
     * @param persistenceService     Mandatory persistenceService optimized for queries based on tenant labels, that is persistence system used for projection data storage.
     * @param tenantsWriteModelStore Mandatory store of tenant as write-model.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public TenantLabelOptimizedProjectionImpl(IDomainRepository<TenantTransactionsCollection> persistenceService, IDomainStore<Tenant> tenantsWriteModelStore) throws IllegalArgumentException {
        if (persistenceService == null) throw new IllegalArgumentException("persistenceService parameter is required!");
        this.persistenceLayer = persistenceService;
        this.tenantsWriteModelStore = tenantsWriteModelStore;
    }

    /**
     * Event interpretation and dispatch to dedicated methods ensuring the projection refresh.
     *
     * @param tenantChanged Committed change. Ignored when null or unidentified event type attribute.
     */
    @Override
    public void when(DomainEvent tenantChanged) {
        if (tenantChanged != null) {
            if (ConcreteDomainChangeEvent.class.isAssignableFrom(tenantChanged.getClass())) {
                try {
                    ConcreteDomainChangeEvent evt = (ConcreteDomainChangeEvent) tenantChanged;
                    // Identify the type of change
                    Attribute type = evt.type();
                    // Select the projection specialized method ensuring the update of this read-model projection
                    if (DomainEventType.TENANT_CREATED.name().equals(type.value())) {
                        whenRegistered(evt);
                    }
                    if (DomainEventType.TENANT_CHANGED.name().equals(type.value())) {
                        whenChanged(evt);
                    }
                    if (DomainEventType.TENANT_DELETED.name().equals(type.value())) {
                        whenRemoved(evt);
                    }
                } catch (Exception ime) {
                    logger.log(Level.SEVERE, ime.getMessage(), ime);
                }
            }
        }
    }

    private void whenRegistered(ConcreteDomainChangeEvent evt) throws ImmutabilityException {
        if (evt != null) {
            // Read the domain object's identifier that have been changed
            EntityReference changedWriteModelObjectRef = evt.changedModelElementReference();
            Identifier sourceDomainObjId = evt.changeSourceIdentifier();
            // Select between multiple potential means for changes domain object identification
            if (changedWriteModelObjectRef != null || sourceDomainObjId != null) {
                Identifier detectedDomainObjectChangedId = (changedWriteModelObjectRef != null) ? changedWriteModelObjectRef.getEntity().identified() : sourceDomainObjId;
                // Load initial values of the tenant projected version that is KNOWN BY THE WRITE-MODEL
                Tenant fullRehydratedTenantVersion = this.tenantsWriteModelStore.findEventFrom(detectedDomainObjectChangedId);
                if (fullRehydratedTenantVersion != null) {
                    String labelValue = fullRehydratedTenantVersion.label().getLabel();
                    if (labelValue != null && !labelValue.isEmpty()) {
                        // --- MANAGE READ-MODEL REFRESH ---
                        // Create a new tenant projection
                        TenantTransactionsCollection navigableDimension = new TenantTransactionsCollection(detectedDomainObjectChangedId.value().toString());
                        // Determine the read-model projection data (primarily based on change notification date, else based on last version rehydrated)
                        OffsetDateTime occurredAt = (evt.occurredAt() != null) ? evt.occurredAt() : fullRehydratedTenantVersion.occurredAt();
                        java.util.Date createdAt = (occurredAt != null) ? Date.from(occurredAt.toInstant()) : null;
                        ActivityState lastState = fullRehydratedTenantVersion.status();
                        Boolean activityState = (lastState != null) ? lastState.isActive() : null;

                        // Update only when any projected dimension's attributes changed
                        if (activityState != null && createdAt != null) {
                            // Add the history of the tenant with new value (e.g defined label, status, creation date)
                            // regarding mandatory LABEL key dimension value regarding this read-model projection managed
                            navigableDimension.add(labelValue, activityState, createdAt);

                            // Add tenant projection into the repository
                            this.persistenceLayer.save(navigableDimension);
                        }
                    }
                }
            }
        }
    }

    private void whenChanged(ConcreteDomainChangeEvent evt) throws ImmutabilityException {
        if (evt != null) {
            EntityReference changedWriteModelObjectRef = evt.changedModelElementReference();
            Identifier sourceDomainObjId = evt.changeSourceIdentifier();
            // Select between multiple potential means for changes domain object identification
            if (changedWriteModelObjectRef != null || sourceDomainObjId != null) {
                Identifier detectedDomainObjectChangedId = (changedWriteModelObjectRef != null) ? changedWriteModelObjectRef.getEntity().identified() : sourceDomainObjId;
                // Load initial values of the tenant projected version that is known by the write-model at the date of the notified event
                Tenant fullRehydratedTenantVersion = this.tenantsWriteModelStore.findEventFrom(detectedDomainObjectChangedId);
                if (fullRehydratedTenantVersion != null) {
                    String labelValue = fullRehydratedTenantVersion.label().getLabel();
                    if (labelValue != null && !labelValue.isEmpty()) {
                        // --- MANAGE READ-MODEL REFRESH ---
                        // Find existing tenant projection (dimension) relative to the tenant identifier
                        TenantTransactionsCollection navigableDimension = this.persistenceLayer.factOfId(detectedDomainObjectChangedId);
                        if (navigableDimension != null) {
                            // Read changed tenant properties when are followed by the read-model view
                            // from the Tenant full state latest version since store (e.g via snapshot repository capability)
                            OffsetDateTime occurredAt = (evt.occurredAt() != null) ? evt.occurredAt() : fullRehydratedTenantVersion.occurredAt();
                            java.util.Date createdAt = (occurredAt != null) ? Date.from(occurredAt.toInstant()) : null;
                            ActivityState lastState = fullRehydratedTenantVersion.status();
                            Boolean activityState = (lastState != null) ? lastState.isActive() : null;

                            // Update only when any projected dimension's attributes changed
                            if (activityState != null && createdAt != null) {
                                // Verify if equals dimension is existing before to add new version
                                if (navigableDimension.findExistingEquals(labelValue, activityState, createdAt) == null) {
                                    // Update the history of the tenant with new value (e.g changed label, status, creation date)
                                    // regarding mandatory LABEL key dimension value regarding this read-model projection managed
                                    navigableDimension.add(labelValue, activityState, createdAt);

                                    // Save upgraded tenant projection into the repository
                                    this.persistenceLayer.save(navigableDimension);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void whenRemoved(ConcreteDomainChangeEvent evt) {

    }

    /**
     * Read naming label regarding the changed tenant.
     *
     * @param evt Mandatory event to read.
     * @return A label or null.
     */
    private Attribute labelOf(ConcreteDomainChangeEvent evt) {
        return EventSpecification.findSpecificationByName(AttributeName.TENANT_LABEL.name(), evt.specification());
    }

    /**
     * Read activity state of the changed tenant.
     *
     * @param evt Mandatory event to read.
     * @return True when active tenant. False when not active tenant. Null when unknown status.
     */
    private Attribute activityStatusOf(ConcreteDomainChangeEvent evt) {
        return EventSpecification.findSpecificationByName(AttributeName.ACTIVITY_STATE.name(), evt.specification());
    }

    @Override
    public TenantTransaction findByLabel(String label, Boolean isOperationStatus, ISessionContext ctx) throws IllegalArgumentException {
        // Prepare query based on tenant label filtering
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put(TenantTransactionsCollection.QueryParameter.TENANT_LABEL.name(), label);
        if (isOperationStatus!=null) {
            queryParameters.put(TenantTransactionsCollection.QueryParameter.TENANT_ACTIVITY_STATUS.name(), isOperationStatus.toString());
        }
        // Find results from repository
        List<TenantTransactionsCollection> equalsNamed = this.persistenceLayer.queryWhere(queryParameters, ctx);
        if (equalsNamed != null && !equalsNamed.isEmpty()) {
            // Basically, a same label can't be used for naming of several Tenant, and only one result shall have been found
            if (equalsNamed.size() > 1) {
                // Unique label per tenant rule violation, so notify error
                logger.log(Level.SEVERE, "Violation of unique usage of label (label: " + label + ") into the tenant collections!");
            } else {
                List<TenantTransaction> tenantVersions = equalsNamed.get(0).versions();

                // Select the latest version of tenant (version date comparison based)
                List<TenantTransaction> orderedVersions = new LinkedList<>(tenantVersions);
                orderedVersions.sort(Comparator.comparing(tenant -> tenant.versionedAt));
                // Get latest
                return orderedVersions.get(orderedVersions.size() - 1);
            }
        }
        return null; // Default not found result
    }
}
