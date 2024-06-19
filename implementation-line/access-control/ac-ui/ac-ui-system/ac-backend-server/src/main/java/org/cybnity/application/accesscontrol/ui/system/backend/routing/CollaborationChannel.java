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
     * Public API entry point of capabilities domain regarding access control client->server
     */
    ac_in(ac.label() + ".in"),

    /**
     * Reserved API entry point of capabilities domain regarding access control client -> server
     */
    ac_in_secure(ac_in.label() + ".secure"),

    /**
     * Output point of capabilities domain regarding access control
     * server->client
     */
    ac_out(ac.label() + ".out");

    private final String label;

    CollaborationChannel(String label) {
        this.label = label;
    }

    public String label() {
        return this.label;
    }
}