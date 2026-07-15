package org.cybnity.keycloak.domain.model;

/**
 * Decoration component regarding a Realm resource that include multiple additional customization elements.
 * For example, this type of Realm can include additional resources to be created by the decoration method into Keycloak server according to a default settings.
 *
 * @author olivier
 */
public class RealmWithDefaultExtendedResources extends Realm {

    /**
     * Default constructor of an extended Realm controlled via Builder Pattern.
     * This extended class allow to host specific additional rules (e.g; limited values; security restriction on authorized values) applied to definition of a RealmRepresentation (controlled and maintained by Keycloak external project).
     *
     * @param builder Mandatory builder.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    RealmWithDefaultExtendedResources(RealmWithDefaultExtendedResourcesBuilder builder) throws IllegalArgumentException {
        super(builder);
        // Get dynamic defined attributes as extended customization elements
        this.setBrowserSecurityHeaders(builder.browserSecurityHeaders());
        // Apply complementary customization of default value not already defined dynamically by the builder
        decorate();
    }

    /**
     * Create additional resources required like static default configuration for this realm (e.g; client scopes, roles).
     */
    @Override
    public void decorate() {
        super.decorate();

        // --- REALM SETTINGS

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
