package org.cybnity.keycloak.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Represents a realm resource.
 *
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public class Realm {

    /**
     * Label without any space.
     */
    private final String name;

    /**
     * The current status of this realm.
     */
    private final Status currentStatus;

    /**
     * Default constructor that apply formatting to ensure that label respect the formatting rules.
     *
     * @param aName         Mandatory label.
     * @param currentStatus Optional known operational status of the realm.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public Realm(String aName, Status currentStatus)
            throws IllegalArgumentException {
        this.name = applyKeycloakRealmLabelFormatRequirements(aName);// Set label reformatted
        this.currentStatus = currentStatus;
    }

    /**
     * Apply rules of formatting on a label as required by Keycloak domain.
     * (e.g.; remove any space or special character to be usable into an URL path).
     *
     * @param label Mandatory label to reformat.
     * @return The reformatted label.
     * @throws IllegalArgumentException When parameter is missing, null, or empty.
     */
    public String applyKeycloakRealmLabelFormatRequirements(String label) throws IllegalArgumentException {
        if (label == null || label.isEmpty())
            throw new IllegalArgumentException("The name parameter is required!");

        // Remove any existing space
        return label.trim();

        // Remove any potential special character
        // TODO implement a regex to remove any special character potentially included into the label

    }

    /**
     * Get the current status of this realm.
     *
     * @return A status or null when unknown.
     */
    public Status currentStatus() {
        return currentStatus;
    }

    /**
     * Get the name of this realm.
     *
     * @return A label respecting the formatting conformity.
     */
    public String name() {
        return name;
    }

    /**
     * State relative to a realm lifecycle.
     */
    public enum Status {
        /**
         * Active state of a realm which is operational and managed by Keycloak.
         */
        REALM_ENABLED,

        /**
         * Inactive state of a realm which is existing in Keycloak but that is not operational (e.g; temporary disabled for maintenance operations and/or security concern).
         */
        REALM_DISABLED;
    }

}
