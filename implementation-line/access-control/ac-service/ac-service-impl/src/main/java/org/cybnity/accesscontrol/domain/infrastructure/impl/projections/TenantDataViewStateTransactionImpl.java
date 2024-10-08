package org.cybnity.accesscontrol.domain.infrastructure.impl.projections;

import org.cybnity.accesscontrol.domain.infrastructure.impl.projections.change.ChangedTenantDataViewVersion;
import org.cybnity.accesscontrol.domain.infrastructure.impl.projections.read.FindTenantDataViewVersionByEqualsLabel;
import org.cybnity.accesscontrol.domain.service.api.model.TenantDataView;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.*;
import org.cybnity.framework.domain.event.ConcreteDomainChangeEvent;
import org.cybnity.framework.domain.event.EventSpecification;
import org.cybnity.framework.domain.event.IEventType;
import org.cybnity.framework.domain.infrastructure.IDomainRepository;
import org.cybnity.framework.domain.infrastructure.IDomainStore;
import org.cybnity.framework.domain.model.IDomainModel;
import org.cybnity.framework.domain.model.ITransactionStateObserver;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.infrastructure.technical.registry.repository.impl.janusgraph.AbstractDomainGraphImpl;
import org.cybnity.infrastructure.technical.registry.repository.impl.janusgraph.projection.AbstractGraphDataViewTransactionImpl;

import java.util.Optional;

/**
 * Tenant data view (domain object data view projected) supporting a TenantDataView vertex type's lifecycle (e.g creation, upgrade/refresh, enhancement, remove) via transactions onto a graph.
 */
public class TenantDataViewStateTransactionImpl extends AbstractGraphDataViewTransactionImpl {

    /**
     * Label designing this type of data view lifecycle manager.
     * It's a logical definition (e.g query name, projection finality unique name) of this projection that can be used for projections equals validation.
     */
    public static final String LABEL = TenantDataView.class.getSimpleName();

    /**
     * Default constructor regarding a graph read model projection.
     *
     * @param ownership              Mandatory domain which is owner of the projection (as in its scope of responsibility).
     * @param dataModel              Mandatory database model that can be manipulated by this transaction about its data view(s).
     * @param observer               Optional observer of the transaction state evolution (e.g to be notified about progress or end of performed transaction).
     * @param tenantsWriteModelStore Mandatory rehydration responsible for tenant domain objects.
     * @throws IllegalArgumentException When any mandatory parameter is missing.
     */
    public TenantDataViewStateTransactionImpl(IDomainModel ownership, AbstractDomainGraphImpl dataModel, ITransactionStateObserver observer, IDomainStore<Tenant> tenantsWriteModelStore) throws IllegalArgumentException {
        super(LABEL, ownership, dataModel, observer, tenantsWriteModelStore); // Define graph manipulable
    }

    @Override
    protected void initSupportedTransactions() {
        IDomainStore<Tenant> observedStore = (IDomainStore<Tenant>) this.getWriteModelStore();

        // Define the transaction supporting the global view perimeter
        IProjectionTransaction tx = new ChangedTenantDataViewVersion(this, this.graphModel(), observedStore); // About created Tenant
        for (IEventType type : tx.observerOf()) {
            supportedTransactions().put(type.name(), tx);
        }

        // and relative to the write-model changes observed...
        if (observedStore != null && ISubscribable.class.isAssignableFrom(observedStore.getClass())) {
            // So attach monitoring capability to the domain objects store for changes detection activation
            // when observable perimeter
            ISubscribable observedWriteStore = (ISubscribable) observedStore;
            observedWriteStore.subscribe(this);// Register read-model projection set as observed of any store change
        }
    }

    @Override
    protected void initSupportedQueries() {
        // Define the read operation supporting the global view perimeter
        IProjectionRead op = new FindTenantDataViewVersionByEqualsLabel(this, this.graphModel());
        for (IEventType type : op.observerOf()) {
            supportedQueries().put(type.name(), op);
        }
    }

    @Override
    public Class<?> subscribeToEventType() {
        return DomainEvent.class; // Domain event type that is source of interest for this transaction monitoring the confirmation of created Access Control domain object to decide creation/upgrade of write-model instance into the data-view model.
        // Else can return a specific event type relative to the unique supported by the handleEvent(...) method
        // Else null to indicate that is interested to be handled for any type of domain event
    }

    /**
     * This method execution is based on any explicit call which is not based on an automatic write-model change detection.
     * Execute the explicit and requested command (e.g data view creation, change or procedure execution with or without collect of results).
     *
     * @param command Mandatory change or query command (CQRS pattern's input element) relative to the projection that can be performed.
     * @return Provider of optional data-view status collected as request results.
     * @throws IllegalArgumentException      When any mandatory parameter is missing.
     * @throws UnsupportedOperationException When request execution generated an issue (e.g query not supported by this projection; or error of request parameter types).
     * @throws UnoperationalStateException   When query execution technical problem occurred.
     */
    @Override
    public IQueryResponse when(Command command) throws IllegalArgumentException, UnsupportedOperationException, UnoperationalStateException {
        if (command == null) throw new IllegalArgumentException("Command parameter is required!");
        try {
            // Identify the type of event which should be source of interest (or not) regarding this projections managed perimeter
            // Normally interpretation of event can be based on its specific domain type (e.g concrete domain event type), or based on specific specification attribute read from event, to detect the source of interest
            // Here, for example, this implementation check the type of attribute relative to the type of origin domain object concerned by the domain command
            IProjectionRead op;
            ITransactionStateObserver observer = getObserver();
            String queryNameBasedOn = Command.TYPE; // default query name of attribute
            if (observer != null && IDomainRepository.class.isAssignableFrom(observer.getClass())) {
                queryNameBasedOn = ((IDomainRepository<?>) observer).queryNameBasedOn();
            }
            Attribute at = EventSpecification.findSpecificationByName(queryNameBasedOn, command.specification());
            if (at != null) {
                // Identify existing operation to execute about query type
                op = supportedQueries().get(at.value());
                if (op != null) {
                    // Execute the query operation that is interested in the monitored event
                    return op.when(command);
                }
            }
        } catch (Exception e) {
            throw new UnsupportedOperationException(e); // query execution problem
        }
        return Optional::empty; // Default answer
    }

    @Override
    public void handleEvent(DomainEvent evt) {
        if (evt != null) {
            // Identify the type of event which should be source of interest (or not) regarding this projections managed perimeter
            // Normally interpretation of event can be based on its specific domain type (e.g concrete domain event type), or based on specific specification attribute read from event, to detect the source of interest
            // Here, for example, this implementation check the type of attribute relative to the type of origin domain object concerned by the domain event
            IProjectionTransaction tx;
            Attribute at = EventSpecification.findSpecificationByName(ConcreteDomainChangeEvent.TYPE, evt.specification());
            if (at != null) {
                // Identify existing transaction to execute about event type
                tx = supportedTransactions().get(at.value());
                if (tx != null) {
                    // Execute the transaction that is interested in the monitored event
                    tx.when(evt);
                }
            }
        }
    }
}
