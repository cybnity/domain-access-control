package org.cybnity.application.accesscontrol.translator.keycloak.api;

/**
 * Static repository of error codes existing in the Keycloak ontology and API (e.g; HTTP 404)
 * Each enum represent a type of error code defining a type of exception.
 */
public enum KeycloakAPIErrorCode {

    // TODO create all the exception type into the keycloak ontology API that are interpretable and detectable into keycloak messages
    HTTP_404("HTTP", "404");;

    private final String protocol;
    private final String errorCode;

    KeycloakAPIErrorCode(String protocol, String errorCode) throws IllegalArgumentException {
        if (protocol == null || protocol.isEmpty()) {
            throw new IllegalArgumentException("protocol cannot be null or empty");
        }
        if (errorCode == null || errorCode.isEmpty()) {
            throw new IllegalArgumentException("errorCode cannot be null or empty");
        }
        this.protocol = protocol;
        this.errorCode = errorCode;
    }

    public String errorCode() {
        return errorCode;
    }

    public String protocol() {
        return protocol;
    }

}
