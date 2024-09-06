package org.cybnity.accesscontrol.domain.infrastructure.impl.projections;

import org.cybnity.accesscontrol.domain.service.api.model.TenantDataView;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.infrastructure.technical.registry.repository.impl.janusgraph.AbstractDomainGraphImpl;
import org.janusgraph.core.schema.JanusGraphManagement;

/**
 * Graph covering the Access Control domain and scope of Vertex types managed in this area (e.g Tenants, in relation with other subdomains).
 * Maintain here the definition of the AC subdomain's graph elements (data view as vertice with unique name) that can be navigated over transversal according to their relations (edges), their properties (vertice property like view type's label).
 */
public class AccessControlDomainGraphImpl extends AbstractDomainGraphImpl {

    /**
     * Domain graph label supporting the Access Control queryable read-models.
     */
    private static final String GRAPH_NAME = "Access Control subdomain";

    /**
     * Default constructor.
     *
     * @param ctx Mandatory context.
     * @throws UnoperationalStateException When problem during context usage.
     * @throws IllegalArgumentException    When mandatory parameter is missing.
     */
    public AccessControlDomainGraphImpl(IContext ctx) throws UnoperationalStateException, IllegalArgumentException {
        super(ctx);
        // Configure supported capabilities
        this.supportsSchema = true;
        this.supportsTransactions = true;
        this.supportsGeoshape = true;
    }

    @Override
    protected void createProperties(JanusGraphManagement management) throws IllegalArgumentException {
        if (management == null) throw new IllegalArgumentException("management parameter is required!");
    }

    /**
     * Definition of each type of data view managed by this graph.
     * @param management Mandatory management instance to update with vertex labels definition.
     * @throws IllegalArgumentException When management parameter is not defined.
     */
    @Override
    protected void createVertexLabels(JanusGraphManagement management) throws IllegalArgumentException {
        if (management == null) throw new IllegalArgumentException("management parameter is required!");
        // Define specification of each data-view (e.g vertex) that is manipulable into this graph (e.g relative to domain perimeter)
        // Each domain object data-view type (type of vertex) is defined by unique label
        management.makeVertexLabel(TenantDataView.class.getSimpleName()).make();
    }

    @Override
    protected void createEdgeLabels(JanusGraphManagement management) throws IllegalArgumentException {
        if (management == null) throw new IllegalArgumentException("management parameter is required!");
    }

    @Override
    protected void createCompositeIndexes(JanusGraphManagement management) throws IllegalArgumentException {
        if (management == null) throw new IllegalArgumentException("management parameter is required!");
    }

    @Override
    protected void createMixedIndexes(JanusGraphManagement management) throws IllegalArgumentException {
        if (management == null) throw new IllegalArgumentException("management parameter is required!");
    }

    @Override
    public String graphName() {
        return GRAPH_NAME;
    }

}
