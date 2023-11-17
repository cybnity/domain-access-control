package org.cybnity.application.accesscontrol.ui.api;

/**
 * Channel regarding a Capabilities Domain exposed as an entry point via the
 * User Interactions Space.
 */
public enum UICapabilityChannel {

    /**
     * Boundary regarding the capabilities of Access Control domain.
     */
    access_control("ac"),
    ;

    private final String acronym;

    private UICapabilityChannel(String acronym) {
        this.acronym = acronym;
    }

    /**
     * Get the label of acronym regarding this channel.
     *
     * @return An acronym or null.
     */
    public String acronym() {
        return this.acronym;
    }
}
