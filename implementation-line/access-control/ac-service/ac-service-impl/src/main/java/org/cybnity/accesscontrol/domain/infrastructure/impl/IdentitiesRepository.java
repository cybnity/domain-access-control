package org.cybnity.accesscontrol.domain.infrastructure.impl;

import org.cybnity.framework.domain.ISessionContext;
import org.cybnity.framework.domain.infrastructure.IDomainRepository;
import org.cybnity.framework.domain.model.Repository;
import org.cybnity.framework.domain.model.SocialEntity;
import org.cybnity.framework.immutable.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Implementation repository optimized for query regarding Identity objects.
 * This store is delegating persistence services to Identity server via connector (e.g identity server used by Keycloak UIAM).
 */
public class IdentitiesRepository extends Repository implements IDomainRepository<SocialEntity> {

    private static IdentitiesRepository singleton;

    /**
     * Reserved constructor.
     */
    private IdentitiesRepository() {
        super();
    }

    /**
     * Get a repository instance.
     *
     * @return A singleton instance.
     */
    public static IdentitiesRepository instance() {
        if (singleton == null) {
            // Initializes singleton instance
            singleton = new IdentitiesRepository();
        }
        return singleton;
    }

    @Override
    public SocialEntity nextIdentity(ISessionContext ctx) {
        return null;
    }

    @Override
    public SocialEntity factOfId(Identifier identifier, ISessionContext ctx) {
        return null;
    }

    @Override
    public boolean remove(SocialEntity socialEntity, ISessionContext ctx) {
        return false;
    }

    @Override
    public void removeAll(Collection<SocialEntity> collection, ISessionContext ctx) {

    }

    @Override
    public SocialEntity save(SocialEntity socialEntity, ISessionContext ctx) {
        return null;
    }

    @Override
    public void saveAll(Collection<SocialEntity> collection, ISessionContext ctx) {

    }

    @Override
    public List<SocialEntity> queryWhere(Map<String, String> map, ISessionContext iSessionContext) {
        return null;
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
