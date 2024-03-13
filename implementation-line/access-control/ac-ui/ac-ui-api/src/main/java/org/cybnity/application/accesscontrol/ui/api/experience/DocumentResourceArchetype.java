package org.cybnity.application.accesscontrol.ui.api.experience;

/**
 * Referential catalog of UI API document paths exposed for capabilities execution.
 * According to the best practices to use nouns (equals to thing instead of referring to an action based on verb) to represent resources, each resource archetype is categorized (e.g as document, collection, and store).
 * This enum identify a resource document based on a consistent naming convention.
 * A document resource is a singular concept that is akin to an object instance or record.
 * It's a single resource inside a resources collection. A document's state representation typically includes both fields with values and links to other related resources.
 * A "singular" name is used to denote this document resource archetype.
 */
public enum DocumentResourceArchetype {
    /**
     * Organization description (e.g company).
     */
    ORGANIZATION("organization");

    private final String label;

    DocumentResourceArchetype(String label) {
        this.label = label;
    }

    /**
     * Get the document name.
     *
     * @return A resource label.
     */
    public String label() {
        return this.label;
    }
}
