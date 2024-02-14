package org.cybnity.accesscontrol.ciam.domain.infrastructure.impl;

import org.cybnity.accesscontrol.ciam.domain.model.TenantsWriteModel;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.ISessionContext;
import org.cybnity.framework.domain.infrastructure.IDomainStore;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.Identifier;

/**
 * Implementation store optimized for write operations regarding Tenant objects.
 * This store is delegating persistence services to IAM server via connector (e.g Keycloak Rest API).
 */
public class TenantsStore implements IDomainStore<Tenant>, TenantsWriteModel {

    private static TenantsStore singleton;

    /**
     * Reserved constructor.
     */
    private TenantsStore() {
    }

    /**
     * Get a store instance.
     *
     * @return A singleton instance.
     */
    public static TenantsWriteModel getInstance() {
        if (singleton == null) {
            // Initializes singleton instance
            singleton = new TenantsStore();
        }
        return singleton;
    }

    @Override
    public void append(Tenant tenant, ISessionContext iSessionContext) {

    }

    @Override
    public Tenant findEventFrom(Identifier identifier, ISessionContext iSessionContext) {
        return null;
    }

    @Override
    public void append(Tenant tenant) {

    }

    @Override
    public Tenant findEventFrom(Identifier identifier) {
        return null;
    }

    @Override
    public void handle(Command command) throws IllegalArgumentException {

    }

    @Override
    public Tenant createTenant(String organizationName) throws IllegalArgumentException {
        return null;
    }
}
