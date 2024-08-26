package org.cybnity.application.accesscontrol.adapter.api;

import org.cybnity.framework.UnoperationalStateException;

/**
 * Contract regarding Single-Sign On (SSO) capabilities that support identification and authentication of users or systems.
 */
public interface SSOAdapter {

    /**
     * For example, disconnect the adapter from the SSO server.
     */
    void freeResources();

    /**
     * Verify the current status of the adapter as healthy and operable for
     * interactions with the SSO server.
     *
     * @throws UnoperationalStateException When adapter status problem detected.
     */
    void checkHealthyState() throws UnoperationalStateException;
}
