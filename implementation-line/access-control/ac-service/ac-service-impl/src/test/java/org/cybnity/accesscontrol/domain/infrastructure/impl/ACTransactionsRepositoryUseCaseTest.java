// Copyright 2017 JanusGraph Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.cybnity.accesscontrol.domain.infrastructure.impl;

import io.vertx.junit5.VertxExtension;
import org.cybnity.accesscontrol.ContextualizedTest;
import org.cybnity.accesscontrol.domain.service.api.event.ACApplicationQueryName;
import org.cybnity.accesscontrol.domain.service.api.model.TenantDataView;
import org.cybnity.accesscontrol.domain.service.api.model.TenantTransactionsCollection;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.DomainEvent;
import org.cybnity.framework.domain.IdentifierStringBased;
import org.cybnity.framework.domain.event.IEventType;
import org.cybnity.framework.domain.model.DomainEntity;
import org.cybnity.framework.domain.model.IDomainEventSubscriber;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.Entity;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.framework.immutable.ImmutabilityException;
import org.cybnity.infrastructure.technical.registry.adapter.api.event.DataViewEventType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * Test of implemented repository relative to a perimeter of Tenant projections.
 */
@ExtendWith({VertxExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ACTransactionsRepositoryUseCaseTest extends ContextualizedTest {

    private static TenantTransactionCollectionsRepository repo;
    private TenantsStore tenantsStore;

    @BeforeEach
    public void initRepository() throws UnoperationalStateException {
        // Create a store managing streamed messages
        tenantsStore = getPersistenceOrientedStore(true /* With snapshots management capability activated */);

        // Prepare a repository of a Access Control domain managing a read-model projections perimeter
        repo = TenantTransactionCollectionsRepository.instance(getContext(), tenantsStore);
    }

    @AfterEach
    public void cleanResources() throws UnoperationalStateException {
        if (tenantsStore != null) tenantsStore.freeResources();
        if (repo != null) {
            repo.drop(); // Delete previous created schema and records
            repo.freeResources();
        }
        repo = null;
    }

    /**
     * Test that propagation of a write model changed confirmation (e.g domain event relative to an aggregate) is handled by repository,
     * which notify the read-model projections potentially managing a scope of data-view versions (read-model of data view versions relative to the aggregate modified in write-model) and verify if data view version (e.g graph Vertex) is automatically created.
     * Simulate in second step a changed version of the initial aggregate, and verify that its data-view in read-model is automatically upgraded (refreshed view).
     *
     * @throws Exception When problem during test execution.
     */
    @Test
    public void givenChangedDomainObject_whenNotifyWriteModelChangeToRepository_thenReadModelDataViewRefreshed() throws Exception {
        // --- Prepare subscriber allowing control of normally promoted data-view projection change events regarding the manipulated read-model ---
        List<String> toDetect = new LinkedList<>();
        // Declare interest for tenant data-view versions
        toDetect.add(DataViewEventType.DATAVIEW_ADDED.name()); // creation event relative to Tenant (id uniquely defined during tenant construction)
        toDetect.add(DataViewEventType.DATAVIEW_CHANGED.name()); // change event relative to Tenant label update (label set during tenant construction)
        toDetect.add(DataViewEventType.DATAVIEW_CHANGED.name()); // change event relative to Tenant status update (state set during tenant construction)
        EventsCheck checker = new EventsCheck(toDetect);
        repo.subscribe(checker); // Register subscriber

        // Simulate a set of values equals to the original aggregate that shall be created in Tenants store
        // Simulate a request of tenant creation command
        Entity originCreationCommand = new DomainEntity(IdentifierStringBased.generate(null));
        //Command cmd = new ConcreteCommandEvent(originCreationCommand);
        String aggregateLabel = "CYBNITY";
        // Create a tenant instance (generating default activity status and label descriptor)
        Boolean originActivityStatus = Boolean.TRUE;
        Tenant tenant = new Tenant(originCreationCommand, IdentifierStringBased.generate(null), originActivityStatus, aggregateLabel);
        List<DomainEvent> changes = tenant.changeEvents();
        Assertions.assertNotNull(changes);

        Identifier originAggregateId = tenant.identified();
        String originCommitVersion = tenant.getCommitVersion();

        // Simulate (e.g event store subscribed by read-model repository) to the repository normally observer of the write-model changes
        CompletableFuture<Boolean> committed = CompletableFuture.supplyAsync(() -> {
            try {
                // Store a Tenant created into the write-model store (which shall notify the projections repository's read-model to be refreshed)
                tenantsStore.append(tenant, this.sessionCtx);
            } catch (ImmutabilityException | UnoperationalStateException e) {
                throw new RuntimeException(e);
            }
            return Boolean.TRUE;
        });

        if (committed.get()) {
            // Execute query based on label filtering
            Map<String, String> queryParameters = prepareQueryBasedOnLabel(aggregateLabel, TenantDataView.class.getSimpleName(), ACApplicationQueryName.TENANT_VIEW_FIND_BY_LABEL);
            List<TenantTransactionsCollection> results = repo.queryWhere(queryParameters, sessionCtx);

            // Verify if a first version of the data view (projection view relative to the aggregate) have been created into the graph model
            Assertions.assertNotNull(results, "Existing data views collection should have been found!");
            Assertions.assertFalse(results.isEmpty(), "First created data view in collection should have been found!");

            TenantTransactionsCollection recordedDataViewStates = results.get(0);
            List<TenantDataView> views = recordedDataViewStates.versions();
            Assertions.assertNotNull(views, "Existing data view should have been found!");
            Assertions.assertFalse(views.isEmpty(), "Created first data view shall have been found!");

            // Check some data view attributes that should be equals to the original aggregate version
            TenantDataView firstVersion = views.get(0);
            Assertions.assertEquals(originAggregateId.value().toString(), recordedDataViewStates.tenantIdentifier(), "Identifier of data view shall be the id of the original aggregate!");
            Assertions.assertEquals(aggregateLabel, firstVersion.valueOfProperty(TenantDataView.PropertyAttributeKey.LABEL), "Invalid label of data view generated in repository's read-model!");
            // Verify that multiple creation/change events generated during the initial instantiation of the Tenant, have been identified with multiple commit versions and only last collected by the data-view refreshed
            Assertions.assertNotEquals(originCommitVersion, firstVersion.valueOfProperty(TenantDataView.PropertyAttributeKey.COMMIT_VERSION), "Invalid data view commit version because shall have been modified by the last change event of the origin tenant (creation event, label definition event, status definition event)!");
            // Verify activity status that shall have been refreshed into the repository during the next change event notification from store (not defined from the Tenant's origin creation event but notified by the status change event notified)
            Assertions.assertEquals(originActivityStatus, Boolean.valueOf(firstVersion.valueOfProperty(TenantDataView.PropertyAttributeKey.ACTIVITY_STATUS)), "Shall have been modified during the last change event received by repository for data-view refresh!");
        }

        // --- CHANGE NOTIFICATION TO READ-MODEL VERIFICATION ---
        // Verify that each data view change performed on the read-model by the repository, have been notified via change events that were handled by checker
        Assertions.assertTrue(checker.isAllEventsToCheckHaveBeenFound(), checker.notAlreadyChecked.size() + " data view changes had not been notified to subscriber!");
    }

    /**
     * Prepare simple query based on a vertex label and type.
     *
     * @param label          Mandatory.
     * @param domainNodeType Mandatory.
     * @return A parameter set.
     */
    private Map<String, String> prepareQueryBasedOnLabel(String label, String domainNodeType, IEventType queryType) {
        Map<String, String> queryParameters = new HashMap<>();
        // Explicit query name to perform
        queryParameters.put(Command.TYPE, queryType.name());
        // Query filtering criteria definition
        queryParameters.put(TenantDataView.PropertyAttributeKey.LABEL.name(), label); // Search vertex (data-view) node with equals name
        queryParameters.put(TenantDataView.PropertyAttributeKey.DATAVIEW_TYPE.name(), domainNodeType); // Yype of vertex (node type in graph model)
        return queryParameters;
    }

    /**
     * Utility class ensuring handling of data-view changes notified by any projection.
     */
    private static class EventsCheck implements IDomainEventSubscriber<DomainEvent> {

        /**
         * List of event type names to detect
         */
        private final List<String> notAlreadyChecked = new LinkedList<>();

        public EventsCheck(List<String> toCheck) {
            super();
            // Prepare validation container
            notAlreadyChecked.addAll(toCheck);
        }

        @Override
        public void handleEvent(DomainEvent event) {
            if (event != null) {
                // Search and remove any existing from the list of origins to check
                notAlreadyChecked.remove(event.type().value());
            }
        }

        @Override
        public Class<?> subscribeToEventType() {
            return DomainEvent.class;
        }

        public boolean isAllEventsToCheckHaveBeenFound() {
            return this.notAlreadyChecked.isEmpty();
        }
    }
}