package org.cybnity.accesscontrol.ciam.domain.infrastructure.impl;

import org.cybnity.accesscontrol.ciam.domain.model.TenantsWriteModel;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.infrastructure.IDomainStore;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.ImmutabilityException;

import java.util.logging.Logger;

/**
 * Implementation class regarding a write model behavior about Tenants.
 */
public class TenantsWriteModelImpl implements TenantsWriteModel {

    /**
     * Delegated store which is in responsibility of event streams persistence.
     */
    private final IDomainStore<Tenant> persistenceLayer;

    private static TenantsWriteModel singleton;

    private final Logger logger = Logger.getLogger(TenantsWriteModelImpl.class.getName());

    /**
     * Get a write model instance.
     *
     * @param persistenceLayer Mandatory store managing the tenant streams persistence layer.
     * @return A singleton instance.
     * @throws IllegalArgumentException When any mandatory parameter is missing.
     */
    public static TenantsWriteModel instance(IDomainStore<Tenant> persistenceLayer) throws IllegalArgumentException {
        if (singleton == null) {
            // Initializes singleton instance
            singleton = new TenantsWriteModelImpl(persistenceLayer);
        }
        return singleton;
    }

    /**
     * Default constructor.
     *
     * @param persistenceLayer Mandatory store managing the tenant streams persistence layer.
     * @throws IllegalArgumentException When any mandatory parameter is missing.
     */
    private TenantsWriteModelImpl(IDomainStore<Tenant> persistenceLayer) throws IllegalArgumentException {
        if (persistenceLayer == null) throw new IllegalArgumentException("persistenceLayer parameter is required!");
        this.persistenceLayer = persistenceLayer;
    }

    @Override
    public void handle(Command command) throws IllegalArgumentException {
        throw new IllegalArgumentException("not implemented!");
    }

    @Override
    public void add(Tenant tenant) throws IllegalArgumentException, ImmutabilityException, UnoperationalStateException {
        if (tenant == null) throw new IllegalArgumentException("tenant parameter is required!");
        // Add new version  of object into persistence system
        this.persistenceLayer.append(tenant);
    }
}
