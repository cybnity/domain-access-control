package org.cybnity.keycloak.api;

/**
 * Static repository of response codes existing in the Keycloak ontology and API (e.g; HTTP 404 error code).
 * Each enum represent a type of API response code defining for example a type of exception cause or a logical notification of a transaction result.
 * The referential of response code is based on <a href="https://www.keycloak.org/docs-api/latest/rest-api/index.html">Keycloak API specification</a>.
 */
public enum KeycloakAPIResponseCode {

    OK("HTTP", "200"),
    CREATED("HTTP", "201"),
    NO_CONTENT("HTTP", "204"),
    BAD_REQUEST("HTTP", "400"),
    FORBIDDEN("HTTP", "403"),
    NOT_FOUND("HTTP", "404"),
    CONFLICT("HTTP", "409"),
    INTERNAL_SERVER_ERROR("HTTP", "500");

    private final String protocol;
    private final String responseCode;

    KeycloakAPIResponseCode(String protocol, String responseCode) throws IllegalArgumentException {
        if (protocol == null || protocol.isEmpty()) {
            throw new IllegalArgumentException("protocol cannot be null or empty");
        }
        if (responseCode == null || responseCode.isEmpty()) {
            throw new IllegalArgumentException("responseCode cannot be null or empty");
        }
        this.protocol = protocol;
        this.responseCode = responseCode;
    }

    public String responseCode() {
        return responseCode;
    }

    public String protocol() {
        return protocol;
    }

}
