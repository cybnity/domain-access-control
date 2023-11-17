package org.cybnity.application.accesscontrol.ui.system.backend.routing;

/**
 * Channel allowing collaboration between the client side area (e.g reactive
 * event bus) and the server-side services (e.g backend capabilities).
 * <p>
 * When channels are exposed on specific channel, the channel naming pattern applied is <<channel>.<<feature>>
 * </p>
 */
public enum CollaborationChannel {

    /**
     * Generic channel regarding the access control domain usable about
     * notification events
     */
    ac("ac"),

    /**
     * Entry point of capabilities domain regarding access control
     * client->server
     */
    ac_in(ac.label() + ".in"),

    /**
     * Public entry point of public capabilities regarding access control client->server
     */
    ac_in_public(ac_in.label() + ".public"),

    /**
     * Public entry point allowing organization registration as tenant
     */
    ac_in_public_organization_registration(ac_in_public.label() + ".organization-registration"),

    /**
     * Output point of capabilities domain regarding access control
     * server->client
     */
    ac_out(ac.label() + ".out");

    private final String label;

    private CollaborationChannel(String label) {
        this.label = label;
    }

    public String label() {
        return this.label;
    }
}