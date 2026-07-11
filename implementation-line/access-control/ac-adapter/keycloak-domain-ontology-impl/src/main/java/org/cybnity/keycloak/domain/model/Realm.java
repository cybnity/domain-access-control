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
public class Realm extends RealmRepresentation {

    /**
     * Default constructor of Realm controlled via Builder Pattern.
     * This extended class allow to host specific additional rules (e.g; limited values; security restriction on authorized values) applied to definition of a RealmRepresentation (controlled and maintained by Keycloak external project).
     *
     * @param builder Mandatory builder.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    Realm(Builder builder) throws IllegalArgumentException {
        super();
        this.realm = builder.name;
        this.enabled = builder.isEnabled;
        this.sslRequired = builder.sslModeRequired;
        this.bruteForceProtected = builder.bruteForceProtected;
        this.notBefore = builder.notBefore;
        this.eventsEnabled = builder.eventsEnabled;
        this.adminEventsEnabled = builder.adminEventsEnabled;
        this.adminEventsDetailsEnabled = builder.adminEventsDetailsEnabled;
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
     * Builder pattern implementation class allowing to respect the build rules of Keycloak regarding a RealmRepresentation object.
     * See example of value supported at <a href="https://jirutka.github.io/keycloak-json-schema/keycloak-realm-26.json">RealmRepresentation object via JSON</a>.
     */
    public static class Builder {
        public static String SSL_MODE_ALL = "all", SSL_MODE_EXTERNAL = "external", SSL_MODE_NONE = "none";
        private String name;
        private boolean isEnabled;
        private String sslModeRequired;
        private boolean bruteForceProtected, eventsEnabled, adminEventsEnabled, adminEventsDetailsEnabled;
        private int notBefore;

        public Realm build() {
            return new Realm(this);
        }

        /**
         * The name of the Realm.
         * This method only apply basic Keycloak minimum sanitization rule that check is real name is not empty and does not contain space character.
         * To ensure better sanitization of real name, use {@link Realm#applyLabelSanitizationRequirements(String)} method before to clean the real name about multiple special characters generating potential problem for usage into URLs.
         *
         * @param realmName A mandatory defined logical name. The origin value is transformed in lower case by default.
         * @return This builder instance.
         * @throws IllegalArgumentException When realName parameter value is empty or does not respect format rule (e.g; not empty, not blank character).
         */
        public Builder name(String realmName) throws IllegalArgumentException {
            if (realmName == null)
                throw new IllegalArgumentException("The name parameter is required!");

            // Check supported value and authorized formatting rules
            if (realmName.isEmpty()) // Not empty
                throw new IllegalArgumentException("The name value shall not be empty!");

            if (realmName.contains(" ")) // Not blank character
                throw new IllegalArgumentException("The name value shall not contain spaces!");

            this.name = realmName;
            return this;
        }

        /**
         * Is the realm is active.
         *
         * @param isEnabled True when is active.
         * @return This builder instance.
         */
        public Builder enabled(boolean isEnabled) {
            this.isEnabled = isEnabled;
            return this;
        }

        /**
         * Optional mode of SSL requirements.
         * Each realm has an SSL Mode associated with it. The SSL Mode defines the SSL/HTTPS requirements for interacting with the realm.
         * It is highly recommended that you either enable SSL on the Keycloak server itself or on a reverse proxy in front of the Keycloak server.
         *
         * @param sslMode Mode definition (e.g; "all" when Keycloak requires SSL for all IP addresses;
         *                "none" when Keycloak does not require SSL;
         *                "external" when users can interact with Keycloak so long as they stick to private IP addresses like localhost, 127.0.0.1, 10.0.x.x, 192.168.x.x, and 172..16.x.x. If you try to access Keycloak from a non-private IP address you will get an error)
         * @return This builder instance.
         * @throws IllegalArgumentException When sslMode value is not supported value (SSL_MODE_ALL, SSL_MODE_EXTERNAL, SSL_MODE_NONE).
         */
        public Builder sslModeRequired(String sslMode) throws IllegalArgumentException {
            if (sslMode != null) {
                // Check value conformity
                if (!SSL_MODE_ALL.equals(sslMode) && !SSL_MODE_EXTERNAL.equals(sslMode) && !SSL_MODE_NONE.equals(sslMode)) {
                    throw new IllegalArgumentException("The SSL mode parameters are invalid!");
                }
            }
            this.sslModeRequired = sslMode;
            return this;
        }

        public Builder bruteForceProtected(boolean bruteForceProtected) {
            this.bruteForceProtected = bruteForceProtected;
            return this;
        }

        /**
         * Activate the workflow for the given resource type and identifier. Optionally schedule the first step using the notBefore parameter.
         *
         * @param timeInSecondsToScheduleFirstWorkflowStep Optional value representing the time to schedule the first workflow step.
         *                                                 The value is either an integer representing the seconds from now, an integer followed by 'ms' representing milliseconds from now, or an ISO-8601 date string.
         *                                                 The value shall be between the minimum -2147483648 and the maximum 2147483647 as an int32 format.
         * @return Builder instance.
         * @throws IllegalArgumentException When parameter value is not respecting the supported range.
         */
        public Builder notBefore(Integer timeInSecondsToScheduleFirstWorkflowStep) throws IllegalArgumentException {
            if (timeInSecondsToScheduleFirstWorkflowStep != null) {
                if (timeInSecondsToScheduleFirstWorkflowStep > -2147483648 && timeInSecondsToScheduleFirstWorkflowStep < 2147483647) {
                    this.notBefore = timeInSecondsToScheduleFirstWorkflowStep;
                } else {
                    throw new IllegalArgumentException("Invalid parameter value! The value shall be between the minimum -2147483648 and the maximum 2147483647 as an int32 format.");
                }
            }
            return this;
        }

        public Builder eventsEnabled(boolean eventsEnabled) {
            this.eventsEnabled = eventsEnabled;
            return this;
        }

        public Builder adminEventsEnabled(boolean adminEventsEnabled) {
            this.adminEventsEnabled = adminEventsEnabled;
            return this;
        }

        /**
         * Include JSON representation for create and update requests.
         *
         * @param adminEventsDetailsEnabled True for include JSON representation for create and update requests.
         * @return Builder instance.
         */
        public Builder adminEventsDetailsEnabled(boolean adminEventsDetailsEnabled) {
            this.adminEventsDetailsEnabled = adminEventsDetailsEnabled;
            return this;
        }

    }
}