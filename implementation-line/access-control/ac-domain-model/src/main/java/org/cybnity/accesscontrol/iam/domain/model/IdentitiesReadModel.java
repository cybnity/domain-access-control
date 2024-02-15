package org.cybnity.accesscontrol.iam.domain.model;

import org.cybnity.framework.domain.IReadModel;
import org.cybnity.framework.domain.model.SocialEntity;

/**
 * Represents a read-oriented and query optimized repository (also sometimes called Aggregate
 * store, or Aggregate-Oriented database) contract for the identities and access bounded context.
 * Identities are defined about organizations, systems, and users that define a boundary of User Identity & Access Management (UIAM).
 */
public interface IdentitiesReadModel extends IReadModel {

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
