package org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin.config;

import org.cybnity.accesscontrol.ConfigurationStrategy;
import org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin.AdminConfigurationVariable;
import org.cybnity.framework.IContext;
import org.cybnity.keycloak.domain.model.RealmBuilder;
import org.cybnity.keycloak.domain.model.RealmWithDefaultExtendedResourcesBuilder;

/**
 * Strategy concrete class defining the composite of configuration elements regarding a Realm creation.
 * Its define the types and value of mandatory settings required for the creation of a Realm supporting a CYBNITY solution.
 * For example, its allow to unify for all the created Realms what shall be included into a Realm configuration like what types of client dedicated to application modules, what default end-user role(s) to include, what default permissions to open, what integration elements required to be stored by Keycloak...
 */
public class RealmConfigurationStrategy extends ConfigurationStrategy {

    /**
     * Default configuration defining that any Realm is enabled by default.
     * When a realm is disabled, users and clients cannot access the realm (only admin adapter can access it).
     */
    public static boolean ENABLED_BY_DEFAULT = true;

    /**
     * Default configuration defining that SSL is required with scope "all".
     */
    public static String SSL_REQUIRED = RealmBuilder.SSL_MODE_ALL;

    /**
     * Default protection enabled against brute force.
     */
    public static Boolean BRUTE_FORCE_PROTECTED = Boolean.TRUE;

    /**
     * Default enabled events.
     */
    public static Boolean EVENTS_ENABLED = Boolean.TRUE;

    /**
     * Default admin events enabled.
     */
    public static Boolean ADMIN_EVENTS_ENABLED = Boolean.TRUE;

    /**
     * Admin events details are not enabled by default.
     */
    public static Boolean ADMIN_EVENTS_DETAILS_ENABLED = Boolean.FALSE;

    /**
     * Default strategy constructor.
     */
    public RealmConfigurationStrategy() {
        super();
    }

    /**
     * Prepare a Realm as configuration object including default and commons settings.
     *
     * @param ctx  Mandatory context eventually including elements required during the Realm object preparation runtime.
     * @param args Mandatory configuration elements and or logical contents that can be used during the preparation process.
     *             Ordered configuration elements are [real name, isEnabled, sslModeRequired, bruteForceProtected, adminEventsDetailsEnabled, notBefore, frontendUrl]
     * @return The expected Realm instance including common settings.
     * @throws IllegalArgumentException When mandatory parameter is missing or is invalid.
     */
    @Override
    public Object prepare(IContext ctx, Object... args) throws IllegalArgumentException {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        if (args == null || args.length < 6)
            throw new IllegalArgumentException("6 args parameters are required and shall include [real name, isEnabled, sslModeRequired, bruteForceProtected, adminEventsDetailsEnabled, notBefore]!");

        // --- Check each mandatory value for real configuration

        String name = (String) args[0]; //  a realm name is provided as args[0]
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("name parameter is required and shall include [realm name string]!");

        Boolean isEnabled = (Boolean) args[1]; //  realm enabling is provided as args[1]
        if (isEnabled == null) {
            isEnabled = ENABLED_BY_DEFAULT;
        }

        String sslModeRequired = (String) args[2];
        if (sslModeRequired == null) {
            sslModeRequired = SSL_REQUIRED;
        }

        Boolean bruteForceProtected = (Boolean) args[3];
        if (bruteForceProtected == null) {
            bruteForceProtected = BRUTE_FORCE_PROTECTED;
        }

        Boolean adminEventsDetailsEnabled = (Boolean) args[4];
        if (adminEventsDetailsEnabled == null) {
            adminEventsDetailsEnabled = ADMIN_EVENTS_DETAILS_ENABLED;
        }

        Integer notBefore = null;
        try {
            notBefore = (Integer) args[5];
        } catch (IndexOutOfBoundsException e) {
            // Not provided input
        }

        RealmWithDefaultExtendedResourcesBuilder builder = new RealmWithDefaultExtendedResourcesBuilder();
        builder.name(name)
                .enabled(isEnabled)
                .sslModeRequired(sslModeRequired)
                .bruteForceProtected(bruteForceProtected)
                .eventsEnabled(EVENTS_ENABLED)
                .adminEventsEnabled(ADMIN_EVENTS_ENABLED)
                .adminEventsDetailsEnabled(adminEventsDetailsEnabled)
                .notBefore(notBefore);

        String xframeoptions = null;
        try {
            xframeoptions = (String) args[6];
        } catch (IndexOutOfBoundsException e) {
            // Not dynamically provided input, so try to read from static configuration (environment variable based)
            xframeoptions = ctx.get(AdminConfigurationVariable.REALM_DEFAULT_SECURITY_HEADER_XFRAME_OPTIONS);
        }
        // Set extension resources
        builder.xFrameOptions(xframeoptions);

        // TODO continue to create each required CYBNITY layer clients as currently defined in manually procedure for automated way

        return builder.build(); // Prepared Realm instance according to Keycloak values rules and return configured instance
    }
}
