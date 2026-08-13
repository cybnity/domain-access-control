package org.cybnity.keycloak.domain.model;

/**
 * Builder pattern implementation class allowing to respect the build rules of Keycloak regarding a RealmRepresentation object.
 * This class check the authorized values eligible for Realm build according to the format rules supported by Keycloak.
 * See example of value supported at <a href="https://jirutka.github.io/keycloak-json-schema/keycloak-realm-26.json">RealmRepresentation object via JSON</a>.
 *
 * @author olivier
 */
public class RealmBuilder {
    /**
     * All request
     */
    public static String SSL_MODE_ALL = "all";
    /**
     * External request
     */
    public static String SSL_MODE_EXTERNAL = "external";
    /**
     * None request
     */
    public static String SSL_MODE_NONE = "none";

    String name;
    String displayName;
    boolean isEnabled;
    String sslModeRequired;
    boolean isOrganizationEnabled;
    boolean isAdminPermissionsEnabled;
    boolean bruteForceProtected, eventsEnabled, adminEventsEnabled, adminEventsDetailsEnabled;
    int notBefore;

    /**
     * Default constructor
     */
    public RealmBuilder() {
    }

    /**
     * Prepare and return an instance of Realm.
     *
     * @return A realm object including default configuration settings defined.
     */
    public Realm build() {
        return new Realm(this);
    }

    /**
     * The name of the Realm.
     * This method only apply basic Keycloak minimum sanitization rule that check is real name is not empty and does not contain space character.
     * To ensure better sanitization of real name, use {@link Realm#applyNameSanitizationRequirements(String)} method before to clean the real name about multiple special characters generating potential problem for usage into URLs.
     *
     * @param realmName A mandatory defined logical name.
     * @return This builder instance.
     * @throws IllegalArgumentException When realName parameter value is empty or does not respect format rule (e.g; not empty, not blank character).
     */
    public RealmBuilder name(String realmName) throws IllegalArgumentException {
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
     * The display name of page regarding the realm authentication page.
     *
     * @param displayName A label.
     * @return This builder instance.
     */
    public RealmBuilder displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Is the realm is enabled.
     *
     * @param isEnabled True when is enabled.
     * @return This builder instance.
     */
    public RealmBuilder enabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        return this;
    }

    /**
     * Is the realm organization management capability is enabled.
     *
     * @param isEnabled True when is enabled.
     * @return This builder instance.
     */
    public RealmBuilder organizationEnabled(boolean isEnabled) {
        this.isOrganizationEnabled = isEnabled;
        return this;
    }

    /**
     * Is the realm administration permissions management capability is enabled.
     *
     * @param isEnabled True when is enabled.
     * @return This builder instance.
     */
    public RealmBuilder adminPermissionsManagementEnabled(boolean isEnabled) {
        this.isAdminPermissionsEnabled = isEnabled;
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
    public RealmBuilder sslModeRequired(String sslMode) throws IllegalArgumentException {
        if (sslMode != null) {
            // Check value conformity
            if (!SSL_MODE_ALL.equals(sslMode) && !SSL_MODE_EXTERNAL.equals(sslMode) && !SSL_MODE_NONE.equals(sslMode)) {
                throw new IllegalArgumentException("The SSL mode parameter is invalid!");
            }
        }
        this.sslModeRequired = sslMode;
        return this;
    }

    /**
     * Enable or disable the force brute protection for the realm.
     *
     * @param bruteForceProtected If enabled, specify what should happen to the user account if a brute force attack is detected.
     * @return This builder.
     */
    public RealmBuilder bruteForceProtected(boolean bruteForceProtected) {
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
    public RealmBuilder notBefore(Integer timeInSecondsToScheduleFirstWorkflowStep) throws IllegalArgumentException {
        if (timeInSecondsToScheduleFirstWorkflowStep != null) {
            if (timeInSecondsToScheduleFirstWorkflowStep > -2147483648 && timeInSecondsToScheduleFirstWorkflowStep < 2147483647) {
                this.notBefore = timeInSecondsToScheduleFirstWorkflowStep;
            } else {
                throw new IllegalArgumentException("Invalid parameter value! The value shall be between the minimum -2147483648 and the maximum 2147483647 as an int32 format.");
            }
        }
        return this;
    }

    /**
     * Define events enabling.
     *
     * @param eventsEnabled Enabling?
     * @return This builder instance.
     */
    public RealmBuilder eventsEnabled(boolean eventsEnabled) {
        this.eventsEnabled = eventsEnabled;
        return this;
    }

    /**
     * Define admin events enabling.
     *
     * @param adminEventsEnabled Enabling?
     * @return This builder instance.
     */
    public RealmBuilder adminEventsEnabled(boolean adminEventsEnabled) {
        this.adminEventsEnabled = adminEventsEnabled;
        return this;
    }

    /**
     * Include JSON representation for create and update requests.
     *
     * @param adminEventsDetailsEnabled True for include JSON representation for create and update requests.
     * @return Builder instance.
     */
    public RealmBuilder adminEventsDetailsEnabled(boolean adminEventsDetailsEnabled) {
        this.adminEventsDetailsEnabled = adminEventsDetailsEnabled;
        return this;
    }

}

