package org.cybnity.accesscontrol.domain.infrastructure.impl;

import org.cybnity.accesscontrol.iam.domain.model.IdentitiesReadModel;
import org.cybnity.framework.domain.ISessionContext;
import org.cybnity.framework.domain.infrastructure.IDomainRepository;
import org.cybnity.framework.domain.model.SocialEntity;
import org.cybnity.framework.immutable.Identifier;

import java.util.Collection;

/**
 * Implementation repository optimized for query regarding Identity objects.
 * This store is delegating persistence services to Identity server via connector (e.g identity server used by Keycloak UIAM).
 */
public class IdentitiesRepository implements IDomainRepository<SocialEntity>, IdentitiesReadModel {

    private static IdentitiesRepository singleton;

    /**
     * Reserved constructor.
     */
    private IdentitiesRepository() {
    }

    /**
     * Get a repository instance.
     *
     * @return A singleton instance.
     */
    public static IdentitiesReadModel getInstance() {
        if (singleton == null) {
            // Initializes singleton instance
            singleton = new IdentitiesRepository();
        }
        return singleton;
    }

    @Override
    public SocialEntity findByName(String s) throws IllegalArgumentException {
        return null;
    }

    @Override
    public SocialEntity findByName(String s, Class<? extends SocialEntity> aClass) throws IllegalArgumentException {
        return null;
    }

    @Override
    public SocialEntity nextIdentity(ISessionContext iSessionContext) {
        return null;
    }

    @Override
    public SocialEntity factOfId(Identifier identifier, ISessionContext iSessionContext) {
        return null;
    }

    @Override
    public boolean remove(SocialEntity socialEntity, ISessionContext iSessionContext) {
        return false;
    }

    @Override
    public void removeAll(Collection<SocialEntity> collection, ISessionContext iSessionContext) {

    }

    @Override
    public SocialEntity save(SocialEntity socialEntity, ISessionContext iSessionContext) {
        return null;
    }

    @Override
    public void saveAll(Collection<SocialEntity> collection, ISessionContext iSessionContext) {

    }

    @Override
    public SocialEntity nextIdentity() {
        return null;
    }

    @Override
    public SocialEntity factOfId(Identifier identifier) {
        return null;
    }

    @Override
    public boolean remove(SocialEntity socialEntity) {
        return false;
    }

    @Override
    public void removeAll(Collection<SocialEntity> collection) {

    }

    @Override
    public SocialEntity save(SocialEntity socialEntity) {
        return null;
    }

    @Override
    public void saveAll(Collection<SocialEntity> collection) {

    }

}
