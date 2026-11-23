package org.cybnity.application.accesscontrol.adapter.api.model;

import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.DataTransferObject;
import org.cybnity.framework.domain.event.IAttribute;
import org.cybnity.framework.domain.infrastructure.util.DateConvention;

import java.text.DateFormat;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Data Transfer Object representing a Role temp version.
 * Read-Model VIEW of a RBAC Role data view version (e.g; denormalized, limited to specific attributes), shareable out of the Access Control domain.
 * Represent a version of a Role entity, at a moment of its life.
 * Type of DTO implementation which can be used into read-model projection(s) stored/exposed via repository.
 * Usable for transfer between internal and external domain via communication systems.
 * Can be used during a translation betwwen a 3rd-party ontology (e.g; Keycloak ontology Realm object type transformed into CYBNITY ontology Role object type) with the access control domain.
 * Unique timely versioned status of a Role, this is a structured object reserved for data transport.
 */
public class RoleDTO extends DataTransferObject {

    /**
     * Immutable set of DTO properties including a data view version's values.
     * The usage of Set collection type allow to specifically exclude duplicates.
     */
    public final Set<Attribute> attributes;

    /**
     * Default constructor of role version.
     *
     * @param label         Optional label naming the role.
     * @param roleUID       Mandatory identifier of the role.
     * @param versionedAt   Optional date of role version creation. When null, the current date is selected as version of the created role transaction.
     * @param createdAt     Optional date of original role (write model) creation. When null, the date of creation of this data view is automatically set to now.
     * @param commitVersion Optional identifier of the commit version relative to the role change transaction.
     * @throws IllegalArgumentException When any mandatory parameter is missing.
     */
    public RoleDTO(String label, String roleUID, Date versionedAt, Date createdAt, String commitVersion) throws IllegalArgumentException {
        if (roleUID == null || roleUID.isEmpty())
            throw new IllegalArgumentException("TenantUID parameter is required!");

        // Prepare immutable attributes set
        Set<Attribute> s = new HashSet<>();
        // Default type of data-view
        s.add(new Attribute(PropertyAttributeKey.DATAVIEW_TYPE.name(), RoleDTO.class.getSimpleName()));
        s.add(new Attribute(PropertyAttributeKey.IDENTIFIED_BY.name(), roleUID));

        if (label != null && !label.isEmpty()) {
            s.add(new Attribute(PropertyAttributeKey.LABEL.name(), label));
        }

        if (commitVersion != null && !commitVersion.isEmpty()) {
            s.add(new Attribute(PropertyAttributeKey.COMMIT_VERSION.name(), commitVersion));
        }

        DateFormat formatter = DateConvention.dateFormatter(); // Convention selection about any date managed into the read-model projected graph
        if (createdAt != null)
            s.add(new Attribute(PropertyAttributeKey.CREATED.name(), formatter.format(createdAt)));

        // Define view update
        s.add(new Attribute(PropertyAttributeKey.LAST_UPDATED_AT.name(), formatter.format(Objects.requireNonNullElseGet(versionedAt, () -> Date.from(OffsetDateTime.now().toInstant())))));

        // Define the unmodifiable view of this data view's properties
        attributes = Collections.unmodifiableSet(s);
    }

    /**
     * Get the value of an attribute type supported as element of specification of this DTO.
     *
     * @param prop Mandatory key of property to read.
     * @return Value of this DTO attribute or null.
     */
    public String valueOfProperty(PropertyAttributeKey prop) {
        List<Attribute> values = attributes.stream().filter(attr -> attr.name().equals(prop.name())).collect(Collectors.toList());
        return (!values.isEmpty()) ? values.get(0).value() : null;
    }

    /**
     * Equality based on origin role's identifier compared.
     *
     * @param obj To compare.
     * @return True when have equals origin role identifier value.
     */
    @Override
    public boolean equals(Object obj) {
        boolean equalsObject = false;
        if (obj == this) {
            return true;
        } else {
            if (obj instanceof RoleDTO) {
                RoleDTO item = (RoleDTO) obj;
                // Logical equality based on label
                if (item.valueOfProperty(PropertyAttributeKey.IDENTIFIED_BY).equals(this.valueOfProperty(PropertyAttributeKey.IDENTIFIED_BY))) {
                    equalsObject = true;
                }
            }
            return equalsObject;
        }
    }

    /**
     * Attribute type managed by this DTO.
     */
    public enum PropertyAttributeKey implements IAttribute {
        /**
         * Unique identifier of the role (vertex equals to data view type).
         */
        IDENTIFIED_BY,
        /**
         * Label naming the role.
         */
        LABEL,
        /**
         * Role object type (equals to Vertex type).
         */
        DATAVIEW_TYPE,
        /**
         * Date of creation regarding the role.
         */
        CREATED,
        /**
         * Commit version of the role domain object (based on the last change identifier).
         */
        COMMIT_VERSION,
        /**
         * Date of last refresh of data regarding the role (versioning date).
         */
        LAST_UPDATED_AT;
    }
}
