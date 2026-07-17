package org.cybnity.keycloak.domain.model;

/**
 * Utility class providing common sanitization services.
 */
public class Sanitizer {

    /**
     * Remove any special character from string.
     * Sanitization rules applied are:
     * All Non-ASCII Alphanumerics removed (including spaces and underscores)
     * Unicode Alphanumeric removed
     * Underscores, accented letters and special characters removed
     * Dot Characters removed
     *
     * @param label Mandatory text to sanitize.
     * @return The cleaned label value.
     * @throws IllegalArgumentException When label parameter is null.
     */
    public static String removeAllSpecialCharacters(String label) throws IllegalArgumentException {
        if (label == null)
            throw new IllegalArgumentException("The label parameter is required!");
        if (label.isEmpty()) return label; // no need of sanitization (implementation optimization rule)

        // Non-ASCII Alphanumerics (ASCII-Only) or inputs with no alphanumerics
        // Remove all characters that are not ASCII letters (a-z, A-Z) or digits (0-9). This is ideal for use cases requiring strict ASCII compliance (e.g. realm label into URLs).
        String cleaned = label.replaceAll("[^a-zA-Z0-9]", ""); // Remove non-ASCII alphanumerics (including spaces and underscores)

        // Unicode Alphanumeric Sanitization
        // Remove all non-alphanumeric characters from "Café123!üñîcødé" (includes Unicode letters)
        cleaned = cleaned.replaceAll("[^\\p{Alnum}]", ""); // Use \p{Alnum} to include Unicode alphanumerics

        // Remove ALL Unicode non-alphanumerics (including accented letters)
        cleaned = cleaned.replaceAll("\\P{Alnum}", "");

        // Remove underscores and special characters
        cleaned = cleaned.replaceAll("[^\\\\w]|_", "");

        // Remove dots
        cleaned = cleaned.replaceAll("\\.", "");

        return cleaned;
    }
}
