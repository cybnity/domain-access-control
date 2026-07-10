package org.cybnity.application.accesscontrol.translator.keycloak.api;

import java.util.EnumSet;
import java.util.Iterator;

/**
 * Context of interpretation regarding a topic supported by Keycloak.
 * It's a context class which hold data.
 */
public class KeycloakInterpretableContext {

    private EnumSet<KeycloakAPIErrorCode> errorCodesReferential;

    public void setReferential(EnumSet<KeycloakAPIErrorCode> errorCodesReferential) throws IllegalArgumentException {
        this.errorCodesReferential = errorCodesReferential;
    }

    /**
     * Search in text value any exiting error code and return the type of exception when found.
     *
     * @param from Text to analyze to identify a type of exception based on HTTP code number (e.g; "HTTP 404" characters chain)
     * @return Found type of error or null.
     */
    public KeycloakAPIErrorCode searchError(String from) {
        if (from == null || from.isEmpty()) {
            return null; // None detection need from undefined message value
        }
        // Search each referential error code (e.g; based on HTTP <number> string pattern) known by referential from the message
        for (KeycloakAPIErrorCode errorCode : errorCodesReferential) {
            if (from.contains(errorCode.protocol() + " " + errorCode.errorCode())) {
                return errorCode;
            }
        }
        return null; // None found
    }
}
