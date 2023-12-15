package org.cybnity.application.accesscontrol.ui.api;


import org.cybnity.infrastructure.technical.message_bus.adapter.api.NamingConventions;

/**
 * Channel regarding a Capabilities Domain exposed as an entry point via the
 * User Interactions Space.
 */
public enum UICapabilityChannel {

    /**
     * Boundary regarding the capabilities of Access Control domain IO entrypoint.
     */
    access_control_in("ac" + NamingConventions.CHANNEL_NAME_SEPARATOR + "in"),

    /**
     * Boundary regarding the feature managing an organization registration.
     */
    access_control_organization_registration("ac" + NamingConventions.CHANNEL_NAME_SEPARATOR + "organization_registration");

    private final String shortName;

    private UICapabilityChannel(String acronym) {
        this.shortName = acronym;
    }

    /**
     * Get the short label (e.g acronym) regarding this channel.
     *
     * @return An acronym or null.
     */
    public String shortName() {
        return this.shortName;
    }
}
