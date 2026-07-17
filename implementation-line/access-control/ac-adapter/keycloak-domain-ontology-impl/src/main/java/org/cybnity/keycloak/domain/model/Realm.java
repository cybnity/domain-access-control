package org.cybnity.keycloak.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;
import org.keycloak.representations.idm.RealmRepresentation;

/**
 * Represents a realm resource.
 * It's an extended RealmRepresentation that allow control of secured values to ensure CYBNITY / Keycloak compatibility.
 * The origin resource description (managed by Keycloak project managers) is available in API documentation provided by Keycloak at <a href="https://www.keycloak.org/docs-api/latest/rest-api/index.html#RealmRepresentation">RealmRepresentation</a>.
 *
 * @author olivier
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public class Realm extends RealmRepresentation implements ExtendedResourcesDecorator {

    /**
     * Default constructor of Realm controlled via Builder Pattern.
     * This extended class allow to host specific additional rules (e.g; limited values; security restriction on authorized values) applied to definition of a RealmRepresentation (controlled and maintained by Keycloak external project).
     *
     * @param builder Mandatory builder.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    Realm(RealmBuilder builder) throws IllegalArgumentException {
        super();
        if (builder == null) throw new IllegalArgumentException("builder cannot be null");
        // Set the common values provided by the builder
        this.setRealm(builder.name);
        this.setEnabled(builder.isEnabled);
        this.setSslRequired(builder.sslModeRequired);
        this.setBruteForceProtected(builder.bruteForceProtected);
        this.setNotBefore(builder.notBefore);
        this.setEventsEnabled(builder.eventsEnabled);
        this.setAdminEventsEnabled(builder.adminEventsEnabled);
        this.setAdminEventsDetailsEnabled(builder.adminEventsDetailsEnabled);
    }

    /**
     * Apply rules of cleaning (also called Sanitization) on a label as required by Keycloak domain.
     * (e.g.; remove any space or special character to be usable into an URL path).
     *
     * @param label Mandatory label to reformat.
     * @return The label value after cleaning.
     * @throws IllegalArgumentException When label parameter is null.
     */
    public static String applyLabelSanitizationRequirements(String label) throws IllegalArgumentException {
        // Remove any potential special character (ensure all non-alphanumeric characters are removed)
        return Sanitizer.removeAllSpecialCharacters(label);
    }


    /**
     * Apply a decoration of the realm with additional customization elements.
     */
    @Override
    public void decorate() {
        // None decoration by default applied on a basic Realm
    }
}