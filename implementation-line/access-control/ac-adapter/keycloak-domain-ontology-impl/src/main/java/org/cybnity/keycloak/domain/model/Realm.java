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
        return removeAllSpecialCharacters(label);
    }

    /**
     * Remove any special character from string.
     * Sanitization rules applied are:
     * - All Non-ASCII Alphanumerics (example "Hello!@# World123_$%^&*()") removed (including spaces and underscores)
     * - Unicode Alphanumeric (example "Café123!üñîcødé") removed
     * - Underscores, accented letters and special characters (example "user_name123!@#") removed
     * - Dot Characters (e.g., ".") removed
     *
     * @param label Mandatory text to sanitize.
     * @return The cleaned label value.
     * @throws IllegalArgumentException When label parameter is null.
     */
    private static String removeAllSpecialCharacters(String label) throws IllegalArgumentException {
        if (label == null)
            throw new IllegalArgumentException("The label parameter is required!");
        if (label.isEmpty()) return label; // no need of sanitization (implementation optimization rule)

        // Non-ASCII Alphanumerics (ASCII-Only) or inputs with no alphanumerics
        // Remove all characters that are not ASCII letters (a-z, A-Z) or digits (0-9). This is ideal for use cases requiring strict ASCII compliance (e.g. realm label into URLs).
        String cleaned = label.replaceAll("[^a-zA-Z0-9]", ""); // Remove non-ASCII alphanumerics (including spaces and underscores)

        // Unicode Alphanumeric Sanitization
        // Remove all non-alphanumeric characters from "Café123!üñîcødé" (includes Unicode letters)
        cleaned = cleaned.replaceAll("[^\\p{Alnum}]", ""); // Use \p{Alnum} to include Unicode alphanumerics

        // Remove ALL Unicode non-alphanumerics (including accented letters)
        cleaned = cleaned.replaceAll("\\P{Alnum}", "");

        // Remove underscores and special characters
        cleaned = cleaned.replaceAll("[^\\\\w]|_", "");

        // Remove dots
        cleaned = cleaned.replaceAll("\\.", "");

        return cleaned;
    }

    /**
     * Apply a decoration of the realm with additional customization elements.
     */
    @Override
    public void decorate() {
        // None decoration by default applied on a basic Realm
    }
}