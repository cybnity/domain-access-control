package org.cybnity.keycloak.api;

import java.util.EnumSet;

/**
 * Concrete class hosting the knowledge to identify a type of HTTP response code known and managed by the Keycloak context, and allowing to identify aligned type of mapped response type usable by any business logic and-or adapter.
 */
public class ResponseCodeIdentificationExpression implements Expression {
    /**
     * Message to interpret for identification of the HTTP response code to understand and translate into type logical response.
     * Detectable message that can include an HTTP code mention and that can be interpreted.
     */
    private final String message;

    /**
     * Default constructor.
     *
     * @param keycloakAPIMessage Mandatory text message that shall be interpreted for identification of potential included response code.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public ResponseCodeIdentificationExpression(String keycloakAPIMessage) throws IllegalArgumentException {
        if (keycloakAPIMessage == null || keycloakAPIMessage.isEmpty()) {
            throw new IllegalArgumentException("keycloakAPIMessage shall not be null or empty!");
        }
        this.message = keycloakAPIMessage;
    }

    /**
     * Execute interpretation of condition rules according to a defined context.
     * For example, can understand a response code produced by Keycloak via its ontology to identify a type of mapped response types.
     *
     * @param ctx Mandatory context hosting referential data.
     * @return Result of interpretation rule performed. Or null if interpretation process did not find correlation.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    @Override
    public Enum<?> interpret(KeycloakInterpretableContext ctx) throws IllegalArgumentException {
        // Feed the context with referential of response codes known/managed by Keycloak
        ctx.setReferential(EnumSet.allOf(KeycloakAPIResponseCode.class));

        // Execute the search and response type identification process
        return ctx.searchError(this.message);
    }
}
