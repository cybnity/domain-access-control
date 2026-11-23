package org.cybnity.keycloak.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit test regarding detection and identification of a Keycloak logical response code from a text.
 *
 * @author olivier
 */
public class ResponseCodeIdentificationUseCaseTest {

    @Test
    public void given_message_whenIncludingSupportedResponseCode_thenIdentifiedLogicalResponseCode() {
        String message = "a text simulating an exception message that include an HTTP 404 error response!";
        ResponseCodeIdentificationExpression expression = new ResponseCodeIdentificationExpression(message);
        // Attempt identification of potential identifiable response code from message
        KeycloakAPIResponseCode code = (KeycloakAPIResponseCode) expression.interpret(new KeycloakInterpretableContext());
        // Check that good code have been identified from referential
        Assertions.assertNotNull(code);
        Assertions.assertSame(KeycloakAPIResponseCode.NOT_FOUND, code, "invalid identified logical response code!");
    }

    @Test
    public void give_message_whenNoneIncludingSupportedResponseCode_thenNotIdentified() {
        String message = "a text simulating an exception message that include an HTTP 4104 error response!";
        ResponseCodeIdentificationExpression expression = new ResponseCodeIdentificationExpression(message);
        // Attempt identification of potential identifiable response code from message
        KeycloakAPIResponseCode code = (KeycloakAPIResponseCode) expression.interpret(new KeycloakInterpretableContext());
        // Check that none code have been identified from a HTTP code that is not supported by the referential
        Assertions.assertNull(code);
    }
}
