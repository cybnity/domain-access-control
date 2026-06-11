package org.cybnity.application.accesscontrol.translator.keycloak.api.mapper;

import org.cybnity.accesscontrol.domain.model.TenantDTO;
import org.cybnity.keycloak.domain.model.Realm;

/**
 * Utility class ensuring the mapping of data relative to a Keycloak Realm object structure (keycloak domain ontology based) with a Tenant object structure (Access Control domain ontology base).
 */
public class RealmMapper implements KeycloakOntologyMapper {

    /**
     * Default constructor of mapper relative to Keycloak Realm objects.
     * Reserved constructor to only concrete factory of mappers.
     */
    RealmMapper() {
    }

    /**
     * Prepare eventual settings required by the mapper before usage of its transformation methods.
     */
    @Override
    public void prepare() {
        // Define eventual settings required by transformation methods
    }

    /**
     * Translate a Realm object including all its contents, into a TenantDTO version
     *
     * @param realm Mandatory object to transform.
     * @return Translated version of the original object.
     * @throws IllegalArgumentException When any mandatory parameter is missing.
     */
    public TenantDTO toDTO(Realm realm) throws IllegalArgumentException {
        if (realm == null) throw new IllegalArgumentException("Realm is required!");

        // TODO to implement about each existing attributes to feed into the target instance
        return null;
    }

    /**
     *
     * @param dto Mandatory object to transform.
     * @return Translated version of the original object.
     * @throws IllegalArgumentException When any mandatory parameter is missing.
     */
    public Realm toDomain(TenantDTO dto) throws IllegalArgumentException {
        if (dto == null) throw new IllegalArgumentException("Tenant is required!");
        // TODO to implement about each existing attributes to feed into the target instance
        return null;

    }

}
