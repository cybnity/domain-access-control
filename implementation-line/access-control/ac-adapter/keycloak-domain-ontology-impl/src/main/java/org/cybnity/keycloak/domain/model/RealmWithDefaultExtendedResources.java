package org.cybnity.keycloak.domain.model;

/**
 * Decoration component regarding a Realm resource that include multiple additional customization elements.
 * For example, this type of Realm can include additional resources to be created by the decoration method into Keycloak server according to a default settings.
 *
 * @author olivier
 */
public class RealmWithDefaultExtendedResources extends Realm {

    /**
     * Current builder allowing to manage decoration of this extended realm.
     */
    private final RealmWithDefaultExtendedResourcesBuilder builder;

    /**
     * Default constructor of an extended Realm controlled via Builder Pattern.
     * This extended class allow to host specific additional rules (e.g; limited values; security restriction on authorized values) applied to definition of a RealmRepresentation (controlled and maintained by Keycloak external project).
     *
     * @param builder Mandatory builder.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    RealmWithDefaultExtendedResources(RealmWithDefaultExtendedResourcesBuilder builder) throws IllegalArgumentException {
        super(builder);
        this.builder = builder; // provide reusable builder for decoration execution
        // Apply complementary customization of default value not already defined dynamically by the builder
        decorate();
    }

    /**
     * Create additional resources required like static default configuration for this realm (e.g; other systems clients and scopes, default application roles).
     */
    @Override
    public void decorate() {
        // Dynamic defined attributes as extended customization elements

        // --- REALM SETTINGS
        this.setAttributes(builder.generalSettings()); // all general contents represented as attributes
        this.setBrowserSecurityHeaders(builder.browserSecurityHeaders()); // all security defense headers
        this.setOrganizationsEnabled(builder.isOrganizationEnabled); // organization management enabling
        this.setAdminPermissionsEnabled(builder.isAdminPermissionsEnabled); // realm admin permissions management enabling

        // --- OTHER CYBNITY SYSTEMS CLIENTS
        this.setClients(builder.systemsClientConfigurations());

        // --- REALM CLIENT SCOPES

        // --- REALM ROLES

        // --- REALM USERS

        // --- REALM GROUPS

        // --- REALM SESSIONS

        // --- REALM EVENTS

        // --- REALM AUTHENTICATION

        // --- REALM IDENTITY PROVIDERS

        // --- REALM USER FEDERATION
    }

}
