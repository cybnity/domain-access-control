package org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin.config;

import org.cybnity.accesscontrol.ConfigurationStrategy;
import org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin.AdminConfigurationVariable;
import org.cybnity.framework.FileBasedConfigurationSource;
import org.cybnity.framework.IContext;
import org.cybnity.framework.IReadableConfiguration;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.keycloak.domain.model.RealmRoleBuilder;
import org.cybnity.keycloak.domain.model.RealmWithDefaultExtendedResources;
import org.cybnity.keycloak.domain.model.RealmWithDefaultExtendedResourcesBuilder;
import org.keycloak.admin.client.Keycloak;

import java.util.Map;

/**
 * Strategy concrete class defining the composite of configuration elements regarding a Realm creation.
 * Its define the types and value of mandatory settings required for the creation of a Realm supporting a CYBNITY solution.
 * For example, its allow to unify for all the created Realms what shall be included into a Realm configuration like what types of client dedicated to application modules, what default end-user role(s) to include, what default permissions to open, what integration elements required to be stored by Keycloak...
 */
public class RealmConfigurationStrategy extends ConfigurationStrategy {

    private final IContext ctx;

    /**
     * Default strategy constructor.
     *
     * @param ctx Mandatory context eventually including elements required during the Realm object preparation runtime.
     * @throws IllegalArgumentException When missing parameter.
     */
    public RealmConfigurationStrategy(IContext ctx) throws IllegalArgumentException {
        super();
        if (ctx == null) {
            throw new IllegalArgumentException("ctx parameter cannot be null!");
        }
        this.ctx = ctx;
    }

    /**
     * Get a set of default properties usable as default configuration data for Keycloak content generation.
     *
     * @param ctx Mandatory context eventually including elements required during the Realm object preparation runtime.
     * @return A set of default configuration data (based on read Keycloak configuration path as defined by environment variable KEYCLOAK_DEFAULT_CONFIGURATION_FILE_PATH).
     * @throws UnoperationalStateException When found configuration file does not include default properties (e.g; empty file).
     * @throws IllegalArgumentException    When mandatory parameter is missing.
     */
    public Map<String, String> getConfigurationProperties(IContext ctx) throws UnoperationalStateException, IllegalArgumentException {
        FileBasedConfigurationSource defaultConfig = getConfigurationSource(ctx);
        Map<String, String> conf = defaultConfig.getProperties();
        if (conf == null)
            throw new UnoperationalStateException("No default values found from file that are required for realm preparation!");
        return conf;
    }

    /**
     * Get source of default configuration data regarding Keycloak.
     *
     * @param ctx Mandatory context eventually including elements required during the Realm object preparation runtime.
     * @return A default configuration data source (based on read Keycloak configuration path as defined by environment variable KEYCLOAK_DEFAULT_CONFIGURATION_FILE_PATH).
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    private FileBasedConfigurationSource getConfigurationSource(IContext ctx) throws IllegalArgumentException {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        return new FileBasedConfigurationSource(ctx.get(AdminConfigurationVariable.KEYCLOAK_DEFAULT_CONFIGURATION_FILE_PATH /* custom default configuration of keycloak contents */));
    }

    /**
     * Prepare a Realm as configuration object including default and commons settings.
     *
     * @param ctx  Mandatory context eventually including elements required during the Realm object preparation runtime.
     * @param args Mandatory configuration elements and or logical contents that can be used during the preparation process.
     *             Ordered configuration elements are [real name, isEnabled, sslModeRequired, bruteForceProtected, adminEventsDetailsEnabled, notBefore, xframeoptions, frontendUrl, contentSecurityPolicy, displayName]
     * @return The expected Realm instance including common settings.
     * @throws IllegalArgumentException When mandatory parameter is missing or is invalid.
     */
    @Override
    public Object prepare(IContext ctx, Object... args) throws IllegalArgumentException {
        if (ctx == null) throw new IllegalArgumentException("ctx parameter is required!");
        if (args == null || args.length < 5)
            throw new IllegalArgumentException("5 args parameters are required and shall include [real name, isEnabled, sslModeRequired, bruteForceProtected, adminEventsDetailsEnabled]!");

        // --- Check each mandatory value for real configuration
        Map<String, String> conf;
        try {
            conf = getConfigurationProperties(ctx);
        } catch (UnoperationalStateException e) {
            throw new IllegalArgumentException(e);
        }

        Object param;
        String name = (String) args[0]; //  a realm name is provided as args[0]
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("name parameter is required and shall include [realm name string]!");

        param = args[1]; //  realm enabling is provided as args[1]
        Boolean isEnabled;
        if (param == null) {
            isEnabled = Boolean.valueOf(conf.get("REALM_DEFAULT_IS_ENABLED"));
        } else {
            isEnabled = (Boolean) param;
        }

        String sslModeRequired = (String) args[2];
        if (sslModeRequired == null) {
            sslModeRequired = conf.getOrDefault("REALM_DEFAULT_SSL_MODE", null);
        }

        param = args[3];
        Boolean bruteForceProtected;
        if (param == null) {
            bruteForceProtected = Boolean.valueOf(conf.get("REALM_DEFAULT_IS_BRUTE_FORCE_PROTECTED"));
        } else {
            bruteForceProtected = (Boolean) param;
        }

        param = args[4];
        Boolean adminEventsDetailsEnabled;
        if (param == null) {
            adminEventsDetailsEnabled = Boolean.valueOf(conf.get("REALM_DEFAULT_IS_ADMIN_EVENTS_DETAILS_ENABLED"));
        } else {
            adminEventsDetailsEnabled = (Boolean) param;
        }

        Integer notBefore = null;
        try {
            notBefore = (Integer) args[5];
        } catch (Exception e) {
            // Not provided input
        }

        try {
            RealmWithDefaultExtendedResourcesBuilder builder = new RealmWithDefaultExtendedResourcesBuilder(getConfigurationSource(ctx));
            builder.name(name)
                    .enabled(isEnabled)
                    .sslModeRequired(sslModeRequired)
                    .bruteForceProtected(bruteForceProtected)
                    .adminEventsDetailsEnabled(adminEventsDetailsEnabled)
                    .notBefore(notBefore)
                    .eventsEnabled(Boolean.parseBoolean(conf.getOrDefault("REALM_DEFAULT_IS_EVENTS_ENABLED", Boolean.FALSE.toString())))
                    .adminEventsEnabled(Boolean.parseBoolean(conf.getOrDefault("REALM_DEFAULT_IS_ADMIN_EVENTS_ENABLED", Boolean.FALSE.toString())))
                    .organizationEnabled(Boolean.parseBoolean(conf.getOrDefault("REALM_DEFAULT_IS_ORGANIZATION_ENABLED", Boolean.FALSE.toString())))
                    .adminPermissionsManagementEnabled(Boolean.parseBoolean(conf.getOrDefault("REALM_DEFAULT_IS_ADMIN_PERMISSIONS_MGT_ENABLED", Boolean.FALSE.toString())));

            String xframeoptions;
            try {
                param = args[6];
                if (param == null) {
                    xframeoptions = conf.getOrDefault("REALM_DEFAULT_SECURITY_HEADER_XFRAME_OPTIONS", null);
                } else {
                    xframeoptions = (String) param;
                }
            } catch (Exception e) {
                // Not dynamically provided input, so try to read from static configuration (environment variable based)
                xframeoptions = conf.getOrDefault("REALM_DEFAULT_SECURITY_HEADER_XFRAME_OPTIONS", null);
            }
            // Set xframe configuration
            builder.xFrameOptions(xframeoptions);

            String frontendurl;
            try {
                param = args[7];
                if (param == null) {
                    frontendurl = conf.getOrDefault("REALM_DEFAULT_FRONTEND_URL", null);
                } else {
                    frontendurl = (String) param;
                }
            } catch (Exception e) {
                // Not dynamically provided input, so try to read from static configuration (environment variable based)
                frontendurl = conf.getOrDefault("REALM_DEFAULT_FRONTEND_URL", null);
            }
            // Set frontend configuration
            builder.frontEndUrl(frontendurl);

            String contentSecurityPolicy;
            try {
                param = args[8];
                if (param == null) {
                    contentSecurityPolicy = conf.getOrDefault("REALM_DEFAULT_SECURITY_HEADER_CONTENT_SECURITY_POLICY", null);
                } else {
                    contentSecurityPolicy = (String) param;
                }
            } catch (Exception e) {
                // Not dynamically provided input, so try to read from static configuration (environment variable based)
                contentSecurityPolicy = conf.getOrDefault("REALM_DEFAULT_SECURITY_HEADER_CONTENT_SECURITY_POLICY", null);
            }
            // Set content security policy configuration
            builder.contentSecurityPolicy(contentSecurityPolicy);

            String displayName;
            try {
                displayName = (String) args[9];
                builder.displayName(displayName);
            } catch (Exception e) {
                // Not dynamically provided input
            }

            // Add additional configuration elements (as defined into config properties file) to include by default into new realm resource

            return builder.build(); // Prepared Realm instance according to Keycloak values rules and return configured instance
        } catch (UnoperationalStateException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Get the type of helper compatible with the current realm configuration strategy, and that allow to identify the additional resources that a Realm shall be completed.
     *
     * @param keycloak      Mandatory operational client.
     * @param tenantLabel   Mandatory tenant identifier to enhance.
     * @param defaultConfig Mandatory default configuration as provider of configuration elements.
     * @return A helper for creation of default complementary resources.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     * @throws UnoperationalStateException When found configuration file does not include default properties.
     */
    public RealmDefaultComplementaryResourcesProvider realmComplementaryDefaultResourcesHelper(Keycloak keycloak, String tenantLabel, RealmWithDefaultExtendedResources defaultConfig) throws IllegalArgumentException, UnoperationalStateException {
        return new RealmDefaultComplementaryResourcesProvider(keycloak, tenantLabel, defaultConfig, new RealmRoleBuilder(), getConfigurationProperties(ctx));
    }
}
