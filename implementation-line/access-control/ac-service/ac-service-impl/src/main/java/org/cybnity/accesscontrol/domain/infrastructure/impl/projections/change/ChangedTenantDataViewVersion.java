package org.cybnity.accesscontrol.domain.infrastructure.impl.projections.change;

import org.apache.tinkerpop.gremlin.process.traversal.Merge;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.cybnity.accesscontrol.domain.service.api.model.TenantDataView;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.*;
import org.cybnity.framework.domain.event.ConcreteDomainChangeEvent;
import org.cybnity.framework.domain.event.DomainEventFactory;
import org.cybnity.framework.domain.event.DomainEventType;
import org.cybnity.framework.domain.event.IEventType;
import org.cybnity.framework.domain.infrastructure.IDomainStore;
import org.cybnity.framework.domain.infrastructure.util.DateConvention;
import org.cybnity.framework.domain.model.DomainEntity;
import org.cybnity.framework.domain.model.ITransactionStateObserver;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.EntityReference;
import org.cybnity.infrastructure.technical.registry.adapter.api.event.DataViewAttributeName;
import org.cybnity.infrastructure.technical.registry.adapter.api.event.DataViewEventType;
import org.cybnity.infrastructure.technical.registry.repository.impl.janusgraph.AbstractDomainGraphImpl;

import javax.naming.ConfigurationException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class implementing the query language that is supported by the graph model (e.g Gremlin with TinkerPop) for execution of a change directive.
 * This implementation class is listening and interpreting the domain events relative to Tenant changes by the write-model.
 * For change, this process is automatically generating a data view version (e.g when none previous data view is existing in graph model) into the queryable read-model.
 */
public class ChangedTenantDataViewVersion extends AbstractDataViewVersionTransactionImpl implements IProjectionTransaction {

    /**
     * Manipulated graph model.
     */
    private final AbstractDomainGraphImpl graph;

    /**
     * Logger specific to this transaction.
     */
    private static final Logger logger = Logger.getLogger(ChangedTenantDataViewVersion.class.getSimpleName());

    /**
     * Provider of full rehydrated domain objects.
     */
    private final IDomainStore<Tenant> rehydrationStore;

    /**
     * Default constructor.
     *
     * @param notifiable             Optional observer of transaction execution end which can be notified.
     * @param graph                  Mandatory manipulable graph.
     * @param tenantsWriteModelStore Mandatory rehydration responsible for tenant domain objects.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public ChangedTenantDataViewVersion(ITransactionStateObserver notifiable, AbstractDomainGraphImpl graph, IDomainStore<Tenant> tenantsWriteModelStore) throws IllegalArgumentException {
        super(notifiable);
        if (graph == null) throw new IllegalArgumentException("Graph parameter is required!");
        if (tenantsWriteModelStore == null)
            throw new IllegalArgumentException("TenantsWriteModelStore parameter is required!");
        this.graph = graph;
        this.rehydrationStore = tenantsWriteModelStore;
    }

    /**
     * Identify the change event type(s) that are supported by this transaction, as a directive to operate on data-view model.
     *
     * @return A set including only DomainEventType.TENANT_CREATED, DomainEventType.TENANT_CHANGED, DomainEventType.TENANT_DELETED event types as sources of interest.
     */
    @Override
    public Set<IEventType> observerOf() {
        return Set.of(/* Tenant instance created in its write-model */ DomainEventType.TENANT_CREATED, DomainEventType.TENANT_CHANGED, DomainEventType.TENANT_DELETED);
    }

    /**
     * Event interpretation and dispatch to dedicated methods ensuring the projection refresh.
     *
     * @param event Committed change. Ignored when null or unidentified event type attribute.
     */
    @Override
    public void when(DomainEvent event) {
        if (event != null) {
            try {
                // Identify and check that is a supported event type
                Attribute type = event.type();
                // Select the projection specialized method ensuring the update of this read-model projection
                if (DomainEventType.TENANT_CREATED.name().equals(type.value())) {
                    whenCreated(event);
                }
                if (DomainEventType.TENANT_CHANGED.name().equals(type.value())) {
                    whenModified(event);
                }
                if (DomainEventType.TENANT_DELETED.name().equals(type.value())) {
                    whenRemoved(event);
                }
            } catch (UnoperationalStateException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Identify the node type relative to the domain, from the data view.
     * When not identified from event, this method define the static default node type statically.
     *
     * @param view Mandatory event.
     * @return A node type dynamically identified from event, or statically defined as TenantDataView.class.getSimpleName()
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    private String identifyNodeType(TenantDataView view) throws IllegalArgumentException {
        if (view == null) throw new IllegalArgumentException("view parameter is required!");
        String domainNodeType = view.valueOfProperty(TenantDataView.PropertyAttributeKey.DATAVIEW_TYPE);
        if (domainNodeType == null || domainNodeType.isEmpty())
            domainNodeType = TenantDataView.class.getSimpleName();// Default type definition
        return domainNodeType;
    }

    /**
     * Interpret a Tenant creation and create version of data-view optimized for query into the graph database.
     *
     * @param event Handled event.
     * @throws UnoperationalStateException When impossible treatment of event and/or repository refresh.
     */
    private void whenCreated(DomainEvent event) throws UnoperationalStateException {
        if (event != null) {
            // A write-model regarding a Tenant domain aggregate is notified as created

            Transaction tx = null;
            // Open a traversal allowing graph manipulation
            try (GraphTraversalSource source = graph.open()) {
                // Map origin domain object attributes from event to targeted (and normally satisfying completeness) data view type
                TenantDataView expectedView = new TenantDataViewMapper(this.rehydrationStore).convertTo(event);
                String domainNodeType = identifyNodeType(expectedView);

                // Initialize transaction
                tx = source.tx();
                GraphTraversalSource gtx = tx.begin();
                gtx.tx().rollback();// Force refresh of transaction state about potential parallel changes executed on data-view to search

                // --- EXISTENCE CHECK: Before to create a new data view, verify if data view version is not existing about identifiable domain object
                // Search from domain object identifier property
                String originDomainIdentifier = expectedView.valueOfProperty(TenantDataView.PropertyAttributeKey.IDENTIFIED_BY);
                Vertex existingNode = findByTenantId(gtx, domainNodeType, originDomainIdentifier);
                String changeRequestLabel = expectedView.valueOfProperty(TenantDataView.PropertyAttributeKey.LABEL);
                if (existingNode == null) {
                    // Search from equals label
                    existingNode = findByTenantLabel(gtx, domainNodeType, changeRequestLabel);
                }
                if (existingNode != null) {
                    // Attempt node update in place of node creation into the repository
                    whenModified(event);
                    return; // Stop creation process
                }

                DateFormat formatter = DateConvention.dateFormatter(); // Convention selection about any date managed into the read-model projected graph

                // --- Define vertex description to create ---
                // Prepare transaction's subject based on required/existing properties
                GraphTraversal<Vertex, Vertex> dataViewVersion = gtx.addV(/* Vertex label nature */domainNodeType)
                        .property(/* Name property */"name", changeRequestLabel)
                        .property(TenantDataView.PropertyAttributeKey.IDENTIFIED_BY.name(), originDomainIdentifier);

                // Add optional properties
                String creationDate = expectedView.valueOfProperty(TenantDataView.PropertyAttributeKey.CREATED);
                if (creationDate != null && !creationDate.isEmpty())
                    dataViewVersion.property(TenantDataView.PropertyAttributeKey.CREATED.name(), formatter.parse(creationDate));

                String commitVersion = expectedView.valueOfProperty(TenantDataView.PropertyAttributeKey.COMMIT_VERSION);
                if (commitVersion != null && !commitVersion.isEmpty())
                    dataViewVersion.property(TenantDataView.PropertyAttributeKey.COMMIT_VERSION.name(), commitVersion);

                String status = expectedView.valueOfProperty(TenantDataView.PropertyAttributeKey.ACTIVITY_STATUS);
                if (status != null && !status.isEmpty())
                    dataViewVersion.property(TenantDataView.PropertyAttributeKey.ACTIVITY_STATUS.name(),
                            /* Boolean type is not supported natively; see https://docs.janusgraph.org/v0.4/index-backend/search-predicates/#data-type-support */
                            status);

                String updatedAt = expectedView.valueOfProperty(TenantDataView.PropertyAttributeKey.LAST_UPDATED_AT);
                if (updatedAt != null && !updatedAt.isEmpty()) {
                    dataViewVersion.property(TenantDataView.PropertyAttributeKey.LAST_UPDATED_AT.name(), formatter.parse(updatedAt));
                }

                final Vertex dataViewVertex = dataViewVersion.next(); // Execute the transaction creating a new graph vertex
                tx.commit(); // commit execution

                // --- READ-MODEL PROJECTION CHANGE NOTIFICATION ---
                // Prepare data view change event
                DomainEvent dataViewChanged = prepareDataViewNotification(DataViewEventType.DATAVIEW_ADDED,
                        dataViewVertex.id().toString(),
                        originDomainIdentifier,
                        domainNodeType,
                        changeRequestLabel,
                        updatedAt,
                        /* Original event reference that was previous source of this event publication */ event.reference(),
                        /* Identify the element of the domain model which was subject of domain event */
                        (ConcreteDomainChangeEvent.class.isAssignableFrom(event.getClass())) ? ((ConcreteDomainChangeEvent) event).changedModelElementReference() : null);

                // Notify the changed data view status of this projection relative to the transaction monitored
                notifyEvent(dataViewChanged);
            } catch (ConfigurationException ce) {
                // Potentially thrown by graph.open() method
                logger.log(Level.SEVERE, "Impossible graph opening!", ce);
                throw new UnoperationalStateException(ce);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Impossible or inconsistent creation of TenantDataView in read-model database!", e);
                if (graph.isSupportsTransactions() && tx != null) {
                    tx.rollback();
                }
                throw new UnoperationalStateException(e);
            }
        }
    }

    /**
     * Prepare a common event relative to a data view version that have been changed.
     * @param dataViewChangeType Mandatory for event definition.
     * @param dataViewId Mandatory for event definition.
     * @param originDomainObjectUID Mandatory for event definition.
     * @param domainNodeType Mandatory for event definition.
     * @param dataViewNodeName Mandatory for event definition.
     * @param dataViewUpdatedAt Optional for event definition.
     * @param priorCommandRef Optional for event definition.
     * @param changedModelElementRef Optional for event definition.
     * @return A prepared new instance of event uniquely identified.
     * @throws IllegalArgumentException When any mandatory parameter is missing or not defined.
     */
    private DomainEvent prepareDataViewNotification(DataViewEventType dataViewChangeType, String dataViewId, String originDomainObjectUID, String domainNodeType, String dataViewNodeName, String dataViewUpdatedAt, EntityReference priorCommandRef, EntityReference changedModelElementRef) throws IllegalArgumentException {
        if (dataViewChangeType==null) throw new IllegalArgumentException("dataViewChangeType parameter is required!");
        if (dataViewId==null || dataViewId.isEmpty()) throw new IllegalArgumentException("dataViewId parameter is required!");
        if (originDomainObjectUID==null || originDomainObjectUID.isEmpty()) throw new IllegalArgumentException("originDomainObjectUID parameter is required!");
        if (domainNodeType==null || domainNodeType.isEmpty()) throw new IllegalArgumentException("domainNodeType parameter is required!");
        if (dataViewNodeName==null || dataViewNodeName.isEmpty()) throw new IllegalArgumentException("dataViewNodeName parameter is required!");

        // Prepare event relative to the read-model projection perimeter changed (e.g including one or several Vertex, edges, attributes...) that could interest read-model observers
        Collection<Attribute> dataViewChangeDefinition = new HashSet<>(); //  // Can contain set of any technical information (e.g time of update, id of graph element changed) and/or logical information (e.g detail about relation name changed on Vertex)
        dataViewChangeDefinition.add(new Attribute(DataViewAttributeName.DATAVIEW_UID.name(), dataViewId));
        dataViewChangeDefinition.add(new Attribute(DataViewAttributeName.DOMAIN_OBJECT_UID.name(), originDomainObjectUID));
        dataViewChangeDefinition.add(new Attribute(DataViewAttributeName.DATAVIEW_NODE_LABEL.name(), domainNodeType));
        dataViewChangeDefinition.add(new Attribute(DataViewAttributeName.DATAVIEW_NODE_NAME.name(), dataViewNodeName));
        if (dataViewUpdatedAt != null && !dataViewUpdatedAt.isEmpty())
            dataViewChangeDefinition.add(new Attribute(DataViewAttributeName.DATAVIEW_DATE.name(), dataViewUpdatedAt));

        return DomainEventFactory.create(/* Event type relative to view performed operation */ dataViewChangeType.name(),
                /* UUID of change event performed under the transaction */ new DomainEntity(IdentifierStringBased.generate(null)),
                /* Logical information relative to the changed data view vertex and/or attributes, and/or any information about the transaction realized */ dataViewChangeDefinition,
                /* Original event reference that was previous source of this event publication */ priorCommandRef,
                /* Identify the element of the domain model which was subject of domain event */ changedModelElementRef);
    }

    /**
     * Interpret a Tenant update and refresh version of data-view optimized for query into the graph database.
     *
     * @param event Handled event.
     * @throws UnoperationalStateException When impossible treatment of event and/or repository refresh.
     */
    private void whenModified(DomainEvent event) throws UnoperationalStateException {
        if (event != null) {
            // A write-model regarding an existing Tenant domain aggregate is notified as modified
            // So an existing data view shall be updated as read-model projection (data view) from current Tenant version modified
            Transaction tx = null;
            // Open a traversal allowing graph manipulation
            try (GraphTraversalSource source = graph.open()) {
                // Map origin domain object attributes from event to targeted (and normally satisfying completeness) data view type
                TenantDataView expectedView = new TenantDataViewMapper(this.rehydrationStore).convertTo(event);
                String domainNodeType = identifyNodeType(expectedView);

                // Initialize transaction
                tx = source.tx();
                GraphTraversalSource gtx = tx.begin();
                gtx.tx().rollback();// Force refresh of transaction state about potential parallel changes executed on data-view to search

                // --- EXISTENCE CHECK (based on tenant identifier that is immutable): Before to update a data view, verify if data view version is existing about identifiable domain object
                // Search existing tenant data view from the origin domain object's identifier property
                String originDomainIdentifier = expectedView.valueOfProperty(TenantDataView.PropertyAttributeKey.IDENTIFIED_BY);
                Vertex existingNode = findByTenantId(gtx, domainNodeType, originDomainIdentifier);
                String changeRequestLabel = expectedView.valueOfProperty(TenantDataView.PropertyAttributeKey.LABEL);
                if (existingNode != null) {
                    // Existing data view can be refreshed...
                    // --- POTENTIAL NODE LABEL CONFLICTS RULE: logical label re-assigned to another Tenant can be in conflict with another vertex that need to be checked before to accept the update requested
                    // Search if another data view is existing with same label than requested
                    Vertex potentialOtherExistingNodeWithSameLabel = findByTenantLabel(gtx, domainNodeType, changeRequestLabel);
                    if (potentialOtherExistingNodeWithSameLabel != null) {
                        // Check if is equals tenant UID and is not another tenant immutable identifier
                        if (!potentialOtherExistingNodeWithSameLabel.id().equals(existingNode.id())) {
                            // Another node is confirmed like already using/named with same label that can't be upgraded on the event's subject
                            // So ignore the event and stop the upgrade to mitigate the risk of duplicated nodes in the repository
                            return;
                        }
                    }

                    DateFormat formatter = DateConvention.dateFormatter(); // Convention selection about any date managed into the read-model projected graph

                    // --- LAST VERSION CHECK RULE: UPDATE ONLY IF CHANGE DATE IS MORE YOUNG THAN EXISTING NODE VERSION (support potential reception of parallel change event, not ordered and relative to a same domain object that have been upgraded over async method not synchronized)
                    String updatedAt = expectedView.valueOfProperty(TenantDataView.PropertyAttributeKey.LAST_UPDATED_AT);
                    if (updatedAt != null && !updatedAt.isEmpty()) {
                        try {
                            // Verify that change event notified is more young than existing data view version
                            Date existingNodeVersionDatedAs = gtx.V(existingNode).next().value(TenantDataView.PropertyAttributeKey.LAST_UPDATED_AT.name());
                            // Compare existing node version age to the new updated version
                            if (existingNodeVersionDatedAs.compareTo(formatter.parse(updatedAt)) > 0/* greater than*/) {
                                // The existing node is more young and up-to-date than the update event notified
                                // So the current data view version shall not be updated
                                return; // Stop treatment and ignore event
                            }
                        } catch (NoSuchElementException nse) {
                            // Not found date
                        }
                    }

                    // --- UPGRADE TRANSACTION ---
                    Map<Object, Object> selectFilter = new HashMap<>();
                    selectFilter.put(TenantDataView.PropertyAttributeKey.IDENTIFIED_BY.name(), originDomainIdentifier);
                    selectFilter.put(/* vertex nature label*/ T.label, domainNodeType);
                    // Define properties to be updated in existing data view (vertex)
                    Map<Object, Object> updatedProperties = new HashMap<>();
                    // Mandatory properties
                    updatedProperties.put(/* Name property */"name", changeRequestLabel);
                    // Optional properties
                    String creationDate = expectedView.valueOfProperty(TenantDataView.PropertyAttributeKey.CREATED);
                    if (creationDate != null && !creationDate.isEmpty())
                        updatedProperties.put(TenantDataView.PropertyAttributeKey.CREATED.name(), formatter.parse(creationDate));
                    String commitVersion = expectedView.valueOfProperty(TenantDataView.PropertyAttributeKey.COMMIT_VERSION);
                    if (commitVersion != null && !commitVersion.isEmpty())
                        updatedProperties.put(TenantDataView.PropertyAttributeKey.COMMIT_VERSION.name(), commitVersion);
                    String status = expectedView.valueOfProperty(TenantDataView.PropertyAttributeKey.ACTIVITY_STATUS);
                    if (status != null && !status.isEmpty())
                        updatedProperties.put(TenantDataView.PropertyAttributeKey.ACTIVITY_STATUS.name(),
                                /* Boolean type is not supported natively; see https://docs.janusgraph.org/v0.4/index-backend/search-predicates/#data-type-support */
                                status);
                    if (updatedAt != null && !updatedAt.isEmpty()) {
                        updatedProperties.put(TenantDataView.PropertyAttributeKey.LAST_UPDATED_AT.name(), formatter.parse(updatedAt));
                    }

                    // Update the changed domain object attributes into the data view
                    // See https://javadoc.io/doc/org.janusgraph/janusgraph-core/latest/index.html for implementation help
                    final Vertex dataViewVertex = gtx.mergeV(selectFilter).option(Merge.onMatch, updatedProperties).next();
                    tx.commit(); // commit execution

                    // --- READ-MODEL PROJECTION CHANGE NOTIFICATION ---
                    // Prepare data view change event
                    DomainEvent dataViewChanged = prepareDataViewNotification(DataViewEventType.DATAVIEW_CHANGED,
                            dataViewVertex.id().toString(),
                            originDomainIdentifier,
                            domainNodeType,
                            changeRequestLabel,
                            updatedAt,
                            /* Original event reference that was previous source of this event publication */ event.reference(),
                            /* Identify the element of the domain model which was subject of domain event */
                            (ConcreteDomainChangeEvent.class.isAssignableFrom(event.getClass())) ? ((ConcreteDomainChangeEvent) event).changedModelElementReference() : null);

                    // Notify the changed data view status of this projection relative to the transaction monitored
                    notifyEvent(dataViewChanged);
                } else {
                    // Impossible to update a tenant data view that is not previously existing (based on creation command) in the repository
                    // In case of repository dropped, a full rehydration of the repository should be started regarding the tenant
                    // Ignore event
                }
            } catch (ConfigurationException ce) {
                // Potentially thrown by graph.open() method
                logger.log(Level.SEVERE, "Impossible graph opening!", ce);
                throw new UnoperationalStateException(ce);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Impossible or inconsistent creation of TenantDataView in read-model database!", e);
                if (graph.isSupportsTransactions() && tx != null) {
                    tx.rollback();
                }
                throw new UnoperationalStateException(e);
            }
        }
    }

    /**
     * Add search step and return found data view node when existing.
     *
     * @param gtx            Mandatory graph source.
     * @param domainNodeType Mandatory vertex nature label.
     * @param tenantLabel    Mandatory tenant name.
     * @return A found vertex or null.
     */
    private Vertex findByTenantLabel(GraphTraversalSource gtx, String domainNodeType, String tenantLabel) {
        try {
            // Execute query
            return gtx.V().has(T.label /* vertex node label only consulted */, domainNodeType).has("name", tenantLabel).next();
        } catch (NoSuchElementException nse) {
            // Not found result
        }
        return null;
    }

    /**
     * Add search step and return found data view node when existing.
     *
     * @param gtx              Mandatory graph source.
     * @param domainNodeType   Mandatory vertex nature label.
     * @param tenantIdentifier Mandatory tenant identifier.
     * @return A found vertex or null.
     */
    private Vertex findByTenantId(GraphTraversalSource gtx, String domainNodeType, String tenantIdentifier) {
        try {
            // Execute query
            return gtx.V().has(T.label /* vertex node label only consulted */, domainNodeType).has(TenantDataView.PropertyAttributeKey.IDENTIFIED_BY.name(), tenantIdentifier).next();
        } catch (NoSuchElementException nse) {
            // Not found result
        }
        return null;
    }

    /**
     * Interpret a Tenant deleted and delete version of data-view optimized for query into the graph database.
     *
     * @param event Handled event.
     * @throws UnoperationalStateException When impossible treatment of event and/or repository refresh.
     */
    private void whenRemoved(DomainEvent event) throws UnoperationalStateException {
        if (event != null) {
            // A write-model regarding a Tenant domain aggregate is notified as removed
            // So delete data view about existing previous version (in case of history views projections supported, the previous data view versions are maintained/not deleted;
            // ... and only deactivation status is updated into the graph like "archived")
            // TODAY IT'S MAKE NON SENS TO DELETE A TENANT FROM CURRENT IMPLEMENTATION WHEN ACCESS CONTROL DOMAIN IS STOPPING TO SUPPORT DELETED KEYCLOAK REALNAME!
            throw new UnoperationalStateException("to implement!");
        }
    }


}