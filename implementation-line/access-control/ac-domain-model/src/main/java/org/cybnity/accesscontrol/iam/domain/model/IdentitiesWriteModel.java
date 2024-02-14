package org.cybnity.accesscontrol.iam.domain.model;

import org.cybnity.framework.domain.IWriteModel;

/**
 * Represents a persistence-oriented and query optimized repository (also sometimes called Aggregate
 * store, or Aggregate-Oriented database) contract for the identities bounded context.
 * Persistence capabilities regarding social entities as Identities who can be owner of account.
 *
 * @author olivier
 */
public interface IdentitiesWriteModel extends IWriteModel {

    /**
     * Register a new social entity into the Identities registry which is declared as organization (e.g company, realm).
     *
     * @param name Mandatory name (e.g realm name) of the structure identity.
     * @return Instance of new created social entity.
     * @throws IllegalArgumentException When any mandatory parameter is not valid.
     */
    public OrganizationalStructure createOrganizationalStructure(String name) throws IllegalArgumentException;

    /**
     * Register a new identity into the Identities registry which is declared as human person.
     *
     * @param firstName Mandatory person first name.
     * @param lastName  Mandatory person last name.
     * @return Instance of new created social entity.
     * @throws IllegalArgumentException When any mandatory parameter is not valid.
     */
    public Person createPerson(String firstName, String lastName) throws IllegalArgumentException;

    /**
     * Register a new social entity into the Identities registry which is declared as system (e.g device).
     *
     * @param name Mandatory name (e.g device label) of the system identity.
     * @return Instance of new created identity.
     * @throws IllegalArgumentException When any mandatory parameter is not valid.
     */
    public SmartSystem createSystem(String name) throws IllegalArgumentException;
}
