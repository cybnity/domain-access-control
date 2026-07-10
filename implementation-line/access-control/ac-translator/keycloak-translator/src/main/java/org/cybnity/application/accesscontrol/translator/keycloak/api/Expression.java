package org.cybnity.application.accesscontrol.translator.keycloak.api;

/**
 * Expression pattern implementation defining the contract of interpretation that concrete class shall execute.
 */
public interface Expression {

    /**
     * Execute interpretation of condition rules according to a defined context.
     * For example, can understand an error code produced by Keycloak via its ontology to identify a type of mapped exception types.
     *
     * @param ctx Mandatory context hosting referential data.
     * @return Result of interpretation rule performed. Or null if interpretation process did not find correlation.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public Enum<?> interpret(KeycloakInterpretableContext ctx) throws IllegalArgumentException;

}
