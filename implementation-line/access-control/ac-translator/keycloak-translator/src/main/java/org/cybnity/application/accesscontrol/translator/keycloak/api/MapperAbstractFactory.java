package org.cybnity.application.accesscontrol.translator.keycloak.api;

import org.cybnity.application.accesscontrol.translator.keycloak.api.mapper.RealmRepresentationMapper;

/**
 * Factory of mapping helpers allowing translation of object between Keycloak domain ontology (3rd-party project governed) and Access Control domain ontology (CYBNITY project governed).
 * Responsibility of mapper instance creation is for concrete factory (this behavior follows the OCP as Open-Closed principla and SRP as Single-Responsibility Principle).
 *
 */
public abstract class MapperAbstractFactory {

    /**
     * Create an instance of RealmRepresentationMapper relative to Realm object (Keycloak ontology).
     *
     * @return New instance of mapper for Realm objects.
     */
    public abstract RealmRepresentationMapper createRealmRepresentationMapper();

}
