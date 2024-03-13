package org.cybnity.accesscontrol.ciam.domain.infrastructure.impl;

import org.cybnity.framework.domain.DomainEvent;
import org.cybnity.framework.domain.ISessionContext;
import org.cybnity.framework.domain.ISnapshotRepository;
import org.cybnity.framework.domain.infrastructure.IDomainStore;
import org.cybnity.framework.domain.model.EventStore;
import org.cybnity.framework.domain.model.EventStream;
import org.cybnity.framework.domain.model.HydrationCapability;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.framework.immutable.ImmutabilityException;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation store optimized for write operations regarding Tenant objects.
 * This store is delegating persistence services to persistent stream .
 */
public class TenantsStore extends EventStore implements IDomainStore<Tenant>, ISnapshotRepository {

    private static TenantsStore singleton;

    private final Logger logger = Logger.getLogger(TenantsStore.class.getName());

    /**
     * Get a store instance.
     *
     * @return A singleton instance.
     */
    public static TenantsStore instance() {
        if (singleton == null) {
            // Initializes singleton instance
            singleton = new TenantsStore();
        }
        return singleton;
    }

    /**
     * Reserved constructor.
     */
    private TenantsStore() {
    }

    @Override
    public void append(Tenant tenant, ISessionContext ctx) {
        if (tenant != null) {
            // Append in persistent stream
            this.append(tenant);
        }
    }

    @Override
    public void append(Tenant tenant) {
        if (tenant != null) {
            try {
                // Append in persistent stream
                this.appendToStream(tenant.identified(), tenant.changeEvents());
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

    @Override
    public Tenant findEventFrom(Identifier identifier) {
        if (identifier != null) {
            EventStream stream = loadEventStream(identifier.value().toString());

            if (stream != null) {
                // Re-hydrate event from stream
                return Tenant.instanceOf(identifier, stream.getEvents());
            }
        }
        return null;
    }


    @Requirement(reqType = RequirementCategory.Robusteness, reqId = "REQ_ROB_3")
    @Override
    public void appendToStream(Identifier domainEventId, List<DomainEvent> changes) throws IllegalArgumentException, ImmutabilityException {
        if (domainEventId == null) throw new IllegalArgumentException("domainEventId parameter is required!");
        if (changes == null) throw new IllegalArgumentException("changes parameter is required!");
        if (changes.isEmpty()) return; // noting to change on domain event

        // TODO implementation of append to persistent stream
        // TODO identifier si event stream déjà existant concernant dans les tenant de cette version (le réutiliser, ou en créer un nouveau)
        // REQ_ROB_3, REQ_CONS_8 use generic fact table (application-agnostic structure) approach for any type of data type (states persistence database, optimized for insertion, and saving the data changes in one collection per type of fact) with structural version model based on hashed version of 88 characters using 512-bit SHA-2 hash that support base-64

        //@Requirement(reqType = RequirementCategory.Consistency, reqId = "REQ_CONS_8")
        //@Requirement(reqType = RequirementCategory.Robustness, reqId = "REQ_ROB_3")


        // Promote notification to subscribers (e.g read-model repositories) about the change events that have been stored and that can have interested fo data view projections (read-model)
        for (DomainEvent changeEvt : changes) {
            subscribersManager().publish(changeEvt);
        }
        throw new IllegalArgumentException("to implement!");
    }

    @Override
    public EventStream loadEventStream(String id) throws IllegalArgumentException {
        if (id == null) throw new IllegalArgumentException("id parameter is required!");
        // Search event stream according to all event record versions supported (all columns per event record class version)

        // TODO load from store all event regarding this tenant id
        // TODO add snapshot repository load in case of version availability before to load the full events for rehydration
        throw new IllegalArgumentException("to implement!");
    }

    @Override
    public EventStream loadEventStreamAfterVersion(String domainEventId, String snapshotVersion) throws IllegalArgumentException {
        // TODO
        return null;
    }

    @Override
    public EventStream loadEventStream(String id, int skipEvents, int maxCount) throws IllegalArgumentException {
        // TODO load from store all event regarding this tenant id
        // TODO add snapshot repository load in case of version availability before to load the full events for rehydration
        throw new IllegalArgumentException("to implement!");
    }

    @Override
    public HydrationCapability getLatestSnapshotById(String s, String s1) throws IllegalArgumentException {
        return null;
    }

    @Override
    public void saveSnapshot(String s, HydrationCapability hydrationCapability, String s1) throws IllegalArgumentException {

    }
}
