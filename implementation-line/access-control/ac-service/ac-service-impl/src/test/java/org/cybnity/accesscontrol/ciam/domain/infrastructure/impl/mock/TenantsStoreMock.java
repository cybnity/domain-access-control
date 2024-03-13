package org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.mock;

import org.cybnity.framework.domain.ISessionContext;
import org.cybnity.framework.domain.ISnapshotRepository;
import org.cybnity.framework.domain.infrastructure.DomainEventInMemoryStoreImpl;
import org.cybnity.framework.domain.infrastructure.IDomainStore;
import org.cybnity.framework.domain.infrastructure.SnapshotProcessEventStreamPersistenceBased;
import org.cybnity.framework.domain.model.EventRecord;
import org.cybnity.framework.domain.model.EventStream;
import org.cybnity.framework.domain.model.HydrationCapability;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.framework.immutable.ImmutabilityException;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mocked store implementation class reusing a common in-memory store impl.
 * This store is implementing the snapshot capabilities into the origin stream of managed tenants events.
 */
public class TenantsStoreMock extends DomainEventInMemoryStoreImpl implements IDomainStore<Tenant>, ISnapshotRepository {

    /**
     * Accessor allowing direct read of store implementation during unit test execution.
     *
     * @return Store's container instance.
     */
    public ConcurrentHashMap<String, LinkedList<EventRecord>> registries() {
        return super.registries();
    }

    private static TenantsStoreMock singleton;

    private final Logger logger = Logger.getLogger(TenantsStoreMock.class.getName());

    /**
     * Reserved constructor.
     */
    private TenantsStoreMock() {
    }

    /**
     * Get a store instance.
     *
     * @return A singleton instance.
     */
    public static TenantsStoreMock instance() {
        if (singleton ==null) {
            singleton = new TenantsStoreMock();
        }
        return singleton;
    }

    @Override
    public void append(Tenant tenant, ISessionContext ctx) {
        if (tenant != null) {
            this.append(tenant);
        }
    }

    @Override
    public void append(Tenant tenant) {
        if (tenant != null) {
            try {
                int changeCount = tenant.changeEvents().size();

                this.appendToStream(tenant.identified(), tenant.changeEvents());

                // --- SNAPSHOT CREATION COUNTING RULES (based 2 events before to make a snapshot) ---
                if (changeCount > 1) {
                    // Create and save a snapshot in stream in a process execution
                    SnapshotProcessEventStreamPersistenceBased snapshotProcess = new SnapshotProcessEventStreamPersistenceBased(/* streamedEventsProvider*/ this, /* snapshotsPersistenceSystem */this) {
                        @Override
                        protected HydrationCapability getRehydratedInstanceFrom(EventStream eventStream) throws IllegalArgumentException {
                            // Re-hydrate event from stream
                            return Tenant.instanceOf(tenant.identified(), eventStream.getEvents());
                        }
                    };
                    // Generate snapshot and save it into events stream persisted
                    snapshotProcess.generateSnapshot(tenant.identified().value().toString());
                }
            } catch (ImmutabilityException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Tenant findEventFrom(Identifier identifier, ISessionContext ctx) {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        return findEventFrom(identifier);
    }

    /**
     * Get the last version of Tenant full state from its write-model, as last version known when the change identifier was applied.
     * When snapshot repository is existing, this service attempt to find latest snapshot version for re-hydration performance optimization;
     * else read origin event stream for re-hydration of the tenant to search and return.
     *
     * @param identifier Mandatory identifier of the Tenant to load.
     * @return A tenant full state valued.
     */
    @Override
    public Tenant findEventFrom(Identifier identifier) {
        if (identifier != null) {
            // Read potential existing snapshot when available repository
            String snapshotVersion = String.valueOf(Tenant.serialVersionUID());
            HydrationCapability rehydratedVersion = this.getLatestSnapshotById(identifier.value().toString(), snapshotVersion);
            if (rehydratedVersion != null) {
                // Load any events since snapshot was taken
                EventStream stream = super.loadEventStreamAfterVersion(identifier.value().toString(), snapshotVersion);
                if (stream != null) {
                    // Replay these events to update snapshot
                    rehydratedVersion.replayEvents(stream);
                    return (Tenant) rehydratedVersion;// Return rehydrated snapshot based instance
                }
            } else {
                // None available persisted snapshot
                EventStream stream = super.loadEventStream(identifier.value().toString());
                if (stream != null) {
                    // Re-hydrate event from origin stream and return instance
                    return Tenant.instanceOf(identifier, stream.getEvents());
                }
            }
        }
        return null;
    }

    @Override
    public HydrationCapability getLatestSnapshotById(String streamedObjectIdentifier, String eventStreamVersion) throws IllegalArgumentException {
        if (streamedObjectIdentifier == null || streamedObjectIdentifier.isEmpty())
            throw new IllegalArgumentException("streamedObjectIdentifier parameter is required!");
        return null;
    }

    @Override
    public void saveSnapshot(String streamedObjectIdentifier, HydrationCapability snapshot, String eventStreamVersion) throws IllegalArgumentException {
        // Create a record (uniquely identified as change record) including a full state of the Tenant snapshot parameter, and save it into the event stream

    }
}
