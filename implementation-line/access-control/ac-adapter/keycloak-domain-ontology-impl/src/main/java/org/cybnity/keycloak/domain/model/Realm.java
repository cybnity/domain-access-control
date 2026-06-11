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
    private String name;

    /**
     * Default constructor that apply formatting to ensure that label respect the formatting rules.
     *
     * @param aName Mandatory label.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public Realm(String aName)
            throws IllegalArgumentException {
        this.name = conformityFormat(aName);// Set label reformatted
    }

    /**
     * Apply rule of reformat on a label.
     * Remove any space or special character.
     *
     * @param label Mandatory label to reformat.
     * @return The reformatted label.
     * @throws IllegalArgumentException When parameter is missing, null, or empty.
     */
    public String conformityFormat(String label) throws IllegalArgumentException {
        if (label == null || label.isEmpty())
            throw new IllegalArgumentException("The name parameter is required!");

        // Remove any existing space
        return label.trim();

        // Remove any special character
        // TODO implement a regex to remove any special character potentially included into the label

    }

    /**
     * Get the name of this realm.
     *
     * @return A label respecting the formatting conformity.
     */
    public String name() {
        return name;
    }

}
