package org.cybnity.keycloak.api;

import java.util.EnumSet;

/**
 * Context of interpretation regarding a topic supported by Keycloak.
 * It's a context class which hold data.
 */
public class KeycloakInterpretableContext {

    /**
     * Referential of response codes usable into this context.
     */
    private EnumSet<KeycloakAPIResponseCode> responseCodesReferential;

    /**
     * Define referential of response codes.
     *
     * @param responseCodesReferential Mandatory set of codes.
     * @throws IllegalArgumentException When missing parameter.
     */
    public void setReferential(EnumSet<KeycloakAPIResponseCode> responseCodesReferential) throws IllegalArgumentException {
        if (responseCodesReferential == null) {
            throw new IllegalArgumentException("responseCodesReferential parameter is required!");
        }
        this.responseCodesReferential = responseCodesReferential;
    }

    /**
     * Search in text value any exiting response code and return the type of response when found.
     *
     * @param from Text to analyze to identify a type of response based on HTTP code number (e.g; "HTTP 404" characters chain identifiable as error response code).
     * @return Found type of response or null.
     */
    public KeycloakAPIResponseCode searchError(String from) {
        if (from == null || from.isEmpty()) {
            return null; // None detection need from undefined message value
        }
        // Search each referential of supported response codes (e.g; based on HTTP <number> string pattern) known by referential from the message
        for (KeycloakAPIResponseCode responseCode : responseCodesReferential) {
            if (from.contains(responseCode.protocol() + " " + responseCode.responseCode())) {
                return responseCode;
            }
        }
        return null; // None found
    }
}
