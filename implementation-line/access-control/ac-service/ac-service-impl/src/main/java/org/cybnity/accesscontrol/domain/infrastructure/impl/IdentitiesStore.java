package org.cybnity.accesscontrol.domain.infrastructure.impl;

import org.cybnity.accesscontrol.iam.domain.model.IdentitiesWriteModel;
import org.cybnity.accesscontrol.iam.domain.model.OrganizationalStructure;
import org.cybnity.accesscontrol.iam.domain.model.Person;
import org.cybnity.accesscontrol.iam.domain.model.SmartSystem;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.DomainEvent;
import org.cybnity.framework.domain.ISessionContext;
import org.cybnity.framework.domain.infrastructure.IDomainStore;
import org.cybnity.framework.domain.model.EventStore;
import org.cybnity.framework.domain.model.EventStream;
import org.cybnity.framework.domain.model.SocialEntity;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.framework.immutable.ImmutabilityException;

import java.util.List;

/**
 * Implementation store optimized for write operations regarding Identity objects.
 * This store is delegating persistence services to Identity server via connector (e.g Keycloak Rest API).
 */
public class IdentitiesStore extends EventStore implements IDomainStore<SocialEntity>, IdentitiesWriteModel {

    private static IdentitiesStore singleton;

    /**
     * Reserved constructor.
     */
    private IdentitiesStore() {
    }

    /**
     * Get a store instance.
     *
     * @return A singleton instance.
     */
    public static IdentitiesWriteModel instance() {
        if (singleton == null) {
            // Initializes singleton instance
            singleton = new IdentitiesStore();
        }
        return singleton;
    }

    @Override
    public OrganizationalStructure createOrganizationalStructure(String name, ISessionContext ctx) throws IllegalArgumentException {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        throw new IllegalArgumentException("not implemented!");
    }

    @Override
    public Person createPerson(String firstName, String lastName, ISessionContext ctx) throws IllegalArgumentException {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        throw new IllegalArgumentException("not implemented!");
    }

    @Override
    public SmartSystem createSystem(String name, ISessionContext ctx) throws IllegalArgumentException {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");

        throw new IllegalArgumentException("not implemented!");
    }

    @Override
    public void handle(Command command) throws IllegalArgumentException {

    }

    @Override
    public void append(SocialEntity socialEntity, ISessionContext ctx) {


    }

    @Override
    public SocialEntity findEventFrom(Identifier identifier, ISessionContext ctx) {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        return null;
    }

    @Override
    public void append(SocialEntity socialEntity) {

    }

    @Override
    public SocialEntity findEventFrom(Identifier identifier) {
        return null;
    }


    @Override
    public void appendToStream(Identifier domainEventId,  List<DomainEvent> changes) throws IllegalArgumentException, ImmutabilityException {
        if (domainEventId == null) throw new IllegalArgumentException("domainEventId parameter is required!");
        if (changes == null) throw new IllegalArgumentException("changes parameter is required!");
        if (changes.isEmpty()) return; // noting to change on domain event
        // TODO implementation of append to persistent stream
        throw new IllegalArgumentException("to implement!");
    }

    @Override
    public EventStream loadEventStream(String id) throws IllegalArgumentException {
        if (id == null) throw new IllegalArgumentException("id parameter is required!");
        // Search event stream according to all event record versions supported (all columns per event record class version)

        // TODO load from store all event regarding this id
        throw new IllegalArgumentException("to implement!");
    }

    @Override
    public EventStream loadEventStreamAfterVersion(String domainEventId, String snapshotVersion) throws IllegalArgumentException {
        // TODO
        return null;
    }

    @Override
    public EventStream loadEventStream(String id, int skipEvents, int maxCount) throws IllegalArgumentException {
        // TODO load from store all event regarding this id
        throw new IllegalArgumentException("to implement!");
    }
}
