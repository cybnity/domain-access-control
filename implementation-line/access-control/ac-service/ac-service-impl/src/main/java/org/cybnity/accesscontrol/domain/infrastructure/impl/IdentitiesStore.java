package org.cybnity.accesscontrol.domain.infrastructure.impl;

import org.cybnity.accesscontrol.iam.domain.model.IdentitiesWriteModel;
import org.cybnity.accesscontrol.iam.domain.model.OrganizationalStructure;
import org.cybnity.accesscontrol.iam.domain.model.Person;
import org.cybnity.accesscontrol.iam.domain.model.SmartSystem;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.ISessionContext;
import org.cybnity.framework.domain.ValueObject;
import org.cybnity.framework.domain.infrastructure.IDomainStore;
import org.cybnity.framework.domain.infrastructure.ISnapshotRepository;
import org.cybnity.framework.domain.model.IDomainModel;
import org.cybnity.framework.domain.model.SocialEntity;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.framework.immutable.ImmutabilityException;
import org.cybnity.infastructure.technical.persistence.store.impl.redis.DomainResourceStoreRedisImpl;
import org.cybnity.infastructure.technical.persistence.store.impl.redis.PersistentObjectNamingConvention;

/**
 * Implementation store optimized for write operations regarding Identity objects.
 * This store is delegating persistence services to Identity server via connector (e.g Keycloak Rest API).
 */
public class IdentitiesStore extends DomainResourceStoreRedisImpl implements IDomainStore<SocialEntity>, IdentitiesWriteModel {

    private static IdentitiesStore singleton;

    /**
     * Default constructor.
     *
     * @param ctx                   Mandatory context.
     * @param dataOwner             Mandatory domain which is owner of the persisted object types into the store.
     * @param managedObjectCategory Mandatory type of convention applicable for the type of object which is managed by this store.
     * @param snapshotsCapability   Optional snapshots repository able to be used by this store helping to optimize events rehydration.
     * @throws UnoperationalStateException When impossible instantiation of UISAdapter based on context parameter.
     * @throws IllegalArgumentException    When any mandatory parameter is missing.
     */
    public IdentitiesStore(IContext ctx, IDomainModel dataOwner, PersistentObjectNamingConvention.NamingConventionApplicability managedObjectCategory, ISnapshotRepository snapshotsCapability) throws UnoperationalStateException, IllegalArgumentException {
        super(ctx, dataOwner, managedObjectCategory, snapshotsCapability);
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
        throw new IllegalArgumentException("to implement!");
    }

    @Override
    public void append(SocialEntity socialEntity, ISessionContext ctx) throws IllegalArgumentException, ImmutabilityException, UnoperationalStateException {
        if (socialEntity != null) {
            this.append(socialEntity);
        }
    }

    @Override
    public SocialEntity findEventFrom(Identifier identifier, ISessionContext ctx) throws IllegalArgumentException, UnoperationalStateException {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        return findEventFrom(identifier);
    }

    @Override
    public void append(SocialEntity socialEntity) throws IllegalArgumentException, ImmutabilityException, UnoperationalStateException {
        throw new IllegalArgumentException("to implement!");
    }

    @Override
    public SocialEntity findEventFrom(Identifier identifier) {
        throw new IllegalArgumentException("to implement!");
    }


    @Override
    public ValueObject<String> snapshotVersionsStorageNamespace() {
        return null;
    }

}
