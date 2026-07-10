package org.cybnity.application.accesscontrol.translator.keycloak.api.mapper;

import org.cybnity.application.accesscontrol.translator.keycloak.api.MapperAbstractFactory;

/**
 * Concrete factory of mappers relative to Keycloak domain ontology.
 * This component allow build of any Keycloak mapper usable for translation between objects relative to Keycloak domain and object types relative to CYBNITY Access Control domain.
 */
public class KeycloakMapperFactory extends MapperAbstractFactory {

    /**
     * Create an instance of RealmRepresentationMapper relative to Realm object (Keycloak ontology).
     *
     * @return New instance of mapper ready for objects transformations.
     */
    @Override
    public RealmRepresentationMapper createRealmRepresentationMapper() {
        RealmRepresentationMapper mapper = new RealmRepresentationMapper();
        // Execute the eventual mapper setting preparation allowing its usage
        mapper.prepare();
        return mapper; // return prepared mapper.
    }
}
