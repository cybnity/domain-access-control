package org.cybnity.application.accesscontrol.translator.keycloak.api.mapper;

import org.cybnity.application.accesscontrol.adapter.api.model.TenantDTO;
import org.cybnity.framework.domain.AbstractDTOMapper;
import org.keycloak.representations.idm.RealmRepresentation;

import java.time.OffsetDateTime;
import java.util.Date;

/**
 * Utility class ensuring the mapping of data relative to a Keycloak RealmRepresentation object structure (keycloak domain ontology based) with a TenantDTO data view object structure (Access Control domain ontology base).
 */
public class RealmRepresentationMapper extends AbstractDTOMapper<TenantDTO> implements KeycloakOntologyMapper {

    /**
     * Default constructor of mapper relative to Keycloak Realm objects.
     * Reserved constructor to only concrete factory of mappers.
     */
    RealmRepresentationMapper() {
    }

    /**
     * Prepare eventual settings required by the mapper before usage of its transformation methods.
     */
    @Override
    public void prepare() {
        // Define eventual settings required by transformation methods
    }

    /**
     * Transform a data view into Keycloak Realm Representation object.
     *
     * @param dto Mandatory object to transform.
     * @return Translated version of the original object.
     * @throws IllegalArgumentException When any mandatory parameter is missing.
     */
    public RealmRepresentation toDomain(TenantDTO dto) throws IllegalArgumentException {
        if (dto == null) throw new IllegalArgumentException("dto parameter is required!");
        return null;
    }

    /**
     * Translate a Realm object including all its contents, into a TenantDTO version
     *
     * @param realmRepresentation Mandatory object to transform.
     * @return Translated version of the original object.
     * @throws IllegalArgumentException When any mandatory parameter is missing. When parameter instance type is not a RealmRepresentation.
     */
    @Override
    public TenantDTO convertTo(Object realmRepresentation) throws IllegalArgumentException, UnsupportedOperationException {
        if (!(realmRepresentation instanceof RealmRepresentation))
            throw new IllegalArgumentException("RealmRepresentation object type is required!");

        RealmRepresentation origin = (RealmRepresentation) realmRepresentation;
        Date dtoVersionDate = Date.from(OffsetDateTime.now().toInstant());

        return new TenantDTO(origin.isEnabled() /* True when realmRepresentation is enabled and considered like operable status*/
                , origin.getRealm() /* Label equals to the name of the realmRepresentation */
                , dtoVersionDate /* Date of the realm/tenant version as now */
                , origin.getId() /* Keycloak realm internal id based on the resource url*/
                , null /* Keycloak does not provide realm creation date */
                , null /* Keycloak does not provide a commit version identifier relative to the realm change transaction */
        );
        // Define eventual complementary existing in realm representation as attributes to feed into the data view to return
    }
}
