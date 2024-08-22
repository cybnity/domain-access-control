package org.cybnity.accesscontrol.domain.infrastructure.impl.projections.read;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.cybnity.accesscontrol.domain.service.api.event.ACApplicationQueryName;
import org.cybnity.accesscontrol.domain.service.api.model.TenantDataView;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.*;
import org.cybnity.framework.domain.event.EventSpecification;
import org.cybnity.framework.domain.event.IEventType;
import org.cybnity.infrastructure.technical.registry.repository.impl.janusgraph.AbstractDomainGraphImpl;
import org.cybnity.infrastructure.technical.registry.repository.impl.janusgraph.projection.AbstractGraphDataViewTransactionImpl;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Represent a provider of read-model projection type that is optimized for query and read of denormalized versions of Tenant domain object.
 * This projection is optimized for search of Tenants from their labels.
 * A Tenant label is defined as unique into the tenants repository, and is ordered into an ascending approach.
 */
public class FindTenantDataViewVersionByEqualsLabel extends AbstractDataViewVersionTransactionImpl implements IProjectionRead {

    /**
     * Manipulated graph model.
     */
    private final AbstractDomainGraphImpl graph;

    /**
     * Technical logger.
     */
    private final Logger logger = Logger.getLogger(FindTenantDataViewVersionByEqualsLabel.class.getName());

    /**
     * Default constructor.
     *
     * @param notifiable Optional observer of transaction execution end which can be notified.
     * @param graph      Mandatory manipulable graph.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public FindTenantDataViewVersionByEqualsLabel(AbstractGraphDataViewTransactionImpl notifiable, AbstractDomainGraphImpl graph) throws IllegalArgumentException {
        super(notifiable);
        if (graph == null) throw new IllegalArgumentException("Graph parameter is required!");
        this.graph = graph;
    }

    /**
     * Read query events that are supported by this read operation, as a directive to execute on data-view model.
     *
     * @return A set including only AccessControlQueryEventType.TENANT_VIEW_FIND_BY_LABEL event type as source of interest.
     */
    @Override
    public Set<IEventType> observerOf() {
        return Set.of(ACApplicationQueryName.TENANT_VIEW_FIND_BY_LABEL);// Unique supported query type
    }

    /**
     * Event interpretation and dispatch to dedicated methods ensuring the query.
     *
     * @param command Mandatory query command.
     * @throws IllegalArgumentException    When mandatory parameter is missing.
     * @throws UnoperationalStateException When invalid query command that is not supported by this projection query implementation.
     * @throws UnoperationalStateException When execution technical problem (e.g access problem to repository).
     */
    @Override
    public IQueryResponse when(Command command) throws IllegalArgumentException, UnsupportedOperationException, UnoperationalStateException {
        if (command == null) throw new IllegalArgumentException("Command parameter is required!");
        // Check valid query type definition which can be processed by this projection read function
        Attribute queryType = EventSpecification.findSpecificationByName(Command.TYPE, command.specification());
        // Identify event type handled
        IEventType query = (queryType != null) ? Enum.valueOf(/* Referential catalog of query types supported by the repository domain */ ACApplicationQueryName.class, queryType.value()) : null;

        // Verify if event type supported by this transaction
        if (query != null && observerOf().contains(query)) {
            // Declared as source of interest in this query
            if (ACApplicationQueryName.TENANT_VIEW_FIND_BY_LABEL == query) {
                // Dispatch to query implementation method
                return findByLabel(command);
            }
        }
        throw new UnsupportedOperationException("Not supported command type!");
    }

    /**
     * Implementation method of Tenant data view version from equals label.
     *
     * @param command Mandatory origin query.
     * @return Found or empty result.
     * @throws IllegalArgumentException    When any required parameter (e.g TenantDataView.PropertyAttributeKey.LABEL.name(), or TenantDataView.PropertyAttributeKey.DATAVIEW_TYPE.name() ) is missing (e.g filter have not been found into the received command).
     * @throws UnoperationalStateException When execution problem (e.g during graph model consultation).
     */
    private IQueryResponse findByLabel(Command command) throws IllegalArgumentException, UnoperationalStateException {
        // Read query parameter usable as filter for query execution
        Attribute labelFilter = EventSpecification.findSpecificationByName(TenantDataView.PropertyAttributeKey.LABEL.name(), command.specification());

        // Check mandatory label value parameter to search
        if (labelFilter == null || labelFilter.value() == null || labelFilter.value().isEmpty())
            throw new IllegalArgumentException("Invalid transaction parameter (TenantDataView.PropertyAttributeKey.LABEL.name() is required)!");
        String tenantViewNameFilter = labelFilter.value();
        Attribute dataViewType = EventSpecification.findSpecificationByName(TenantDataView.PropertyAttributeKey.DATAVIEW_TYPE.name(), command.specification());

        // Check mandatory domain object type (data view nature) to navigate as queryable projection
        if (dataViewType == null || dataViewType.value() == null || dataViewType.value().isEmpty())
            throw new IllegalArgumentException("Missing mandatory parameter (TenantDataView.PropertyAttributeKey.DATAVIEW_TYPE.name() is required and shall be defined)!");
        // Type of node can be statically defined by the implementation language (like here) or dynamically known by the requester
        String domainNodeType = (dataViewType.value() != null && !dataViewType.value().isEmpty()) ? dataViewType.value() : TenantDataView.class.getSimpleName();

        try (GraphTraversalSource traversal = graph.open()) {
            GraphTraversalSource gtx;
            gtx = traversal.tx().begin();

            gtx.tx().rollback();// Force refresh of transaction state about potential parallel changes executed on data-view to search

            // Execute query implementation according to the query language supported by JanusGraph library
            try {
                // Execute query
                Vertex foundEqualsLabelNode = gtx.V().has(T.label /* vertex node label only consulted */, domainNodeType).has("name", tenantViewNameFilter).next();

                // Mandatory existing properties
                String tenantUID = foundEqualsLabelNode.value(TenantDataView.PropertyAttributeKey.IDENTIFIED_BY.name());
                String label = foundEqualsLabelNode.value("name");
                // Optional existing properties
                Date createdAt = null;
                try {
                    createdAt = foundEqualsLabelNode.value(TenantDataView.PropertyAttributeKey.CREATED.name());
                } catch (Exception nse) {
                    // Unknown optional property
                }
                Date updatedAt = null;
                try {
                    updatedAt = foundEqualsLabelNode.value(TenantDataView.PropertyAttributeKey.LAST_UPDATED_AT.name());
                } catch (Exception nse) {
                    // Unknown optional property
                }
                String commitVersion = null;
                try {
                    commitVersion = foundEqualsLabelNode.value(TenantDataView.PropertyAttributeKey.COMMIT_VERSION.name());
                } catch (Exception nse) {
                    // Unknown optional property
                }
                String status = null;
                try {
                    status = foundEqualsLabelNode.value((TenantDataView.PropertyAttributeKey.ACTIVITY_STATUS.name()));
                } catch (Exception nse) {
                    // Unknown optional property
                }
                Boolean isActive = (status != null && !status.isEmpty()) ? Boolean.valueOf(status) : null;

                final TenantDataView result = new TenantDataView(isActive, label, updatedAt, tenantUID, createdAt, commitVersion);

                // Prepare found data view response
                return () -> Optional.of(result);
            } catch (NoSuchElementException nse) {
                // Not found result
            }
        } catch (Exception e) {
            throw new UnoperationalStateException(e);
        }
        // Confirm none found result
        return Optional::empty; // Default answer
    }
}
