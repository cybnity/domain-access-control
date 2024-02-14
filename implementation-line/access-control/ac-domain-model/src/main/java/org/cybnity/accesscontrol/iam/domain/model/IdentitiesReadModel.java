package org.cybnity.accesscontrol.iam.domain.model;

import org.cybnity.framework.domain.IReadModel;
import org.cybnity.framework.domain.model.SocialEntity;
import org.cybnity.framework.domain.model.Tenant;

/**
 * Represents a read-oriented and query optimized repository (also sometimes called Aggregate
 * store, or Aggregate-Oriented database) contract for the identities and access bounded context.
 * Identities are defined about organizations, systems, and users that define a boundary of User Identity & Access Management (UIAM).
 */
public interface IdentitiesReadModel extends IReadModel {

    /**
     * Search existing tenant which have an equals name relative to a specific label.
     *
     * @param name                Mandatory logical name of tenant to search as owner of a known tenant.
     * @param includingExistUsers Is tenant to search must shall be filtered according any existing valid (e.g registered and activated as verified user account) user accounts already managed? True if tenant to return shall be a tenant that have valid user accounts already activated. False when the search shall ignore the potential existing of valid user accounts activated. Ignored filter when null.
     * @return An identified tenant, or null.
     * @throws IllegalArgumentException When any mandatory parameter is not valid.
     */
    public Tenant findTenant(String name, Boolean includingExistUsers) throws IllegalArgumentException;

    /**
     * Search social entity (e.g company or realm name) logically identified by a specific label.
     *
     * @param name Mandatory name of social entity to find.
     * @return Existing entity named by a label equals to the name. Null when not found.
     * @throws IllegalArgumentException When any mandatory parameter is not defined.
     */
    public SocialEntity findByName(String name) throws IllegalArgumentException;

    /**
     * Search a specific type of social entity logically identified by a specific label.
     *
     * @param name Mandatory name of social entity to find.
     * @param type Optional type of social entity to search only. When null, findByName(String name) is executed without any filter relative to the type of social entity to select only.
     * @return Existing entity named by a label equals to the name. Null when not found.
     * @throws IllegalArgumentException When any mandatory parameter is not defined.
     */
    public SocialEntity findByName(String name, Class<? extends SocialEntity> type) throws IllegalArgumentException;
}
