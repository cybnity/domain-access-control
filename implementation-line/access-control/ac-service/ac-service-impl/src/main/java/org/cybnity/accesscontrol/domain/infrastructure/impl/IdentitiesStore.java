package org.cybnity.accesscontrol.domain.infrastructure.impl;

import org.cybnity.accesscontrol.iam.domain.model.IdentitiesWriteModel;
import org.cybnity.accesscontrol.iam.domain.model.OrganizationalStructure;
import org.cybnity.accesscontrol.iam.domain.model.Person;
import org.cybnity.accesscontrol.iam.domain.model.SmartSystem;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.ISessionContext;
import org.cybnity.framework.domain.infrastructure.IDomainStore;
import org.cybnity.framework.domain.model.SocialEntity;
import org.cybnity.framework.immutable.Identifier;

/**
 * Implementation store optimized for write operations regarding Identity objects.
 * This store is delegating persistence services to Identity server via connector (e.g Keycloak Rest API).
 */
public class IdentitiesStore implements IDomainStore<SocialEntity>, IdentitiesWriteModel {

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
    public static IdentitiesWriteModel getInstance() {
        if (singleton == null) {
            // Initializes singleton instance
            singleton = new IdentitiesStore();
        }
        return singleton;
    }

    @Override
    public OrganizationalStructure createOrganizationalStructure(String s) throws IllegalArgumentException {
        return null;
    }

    @Override
    public Person createPerson(String s, String s1) throws IllegalArgumentException {
        return null;
    }

    @Override
    public SmartSystem createSystem(String s) throws IllegalArgumentException {
        return null;
    }

    @Override
    public void handle(Command command) throws IllegalArgumentException {

    }

    @Override
    public void append(SocialEntity socialEntity, ISessionContext iSessionContext) {

    }

    @Override
    public SocialEntity findEventFrom(Identifier identifier, ISessionContext iSessionContext) {
        return null;
    }

    @Override
    public void append(SocialEntity socialEntity) {

    }

    @Override
    public SocialEntity findEventFrom(Identifier identifier) {
        return null;
    }
}
