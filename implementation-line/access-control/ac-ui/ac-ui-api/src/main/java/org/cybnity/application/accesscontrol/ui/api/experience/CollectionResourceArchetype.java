package org.cybnity.application.accesscontrol.ui.api.experience;

/**
 * Referential catalog of UI API collection paths exposed for capabilities execution.
 * According to the best practices to use nouns (equals to thing instead of referring to an action based on verb) to represent resources, each resource archetype is categorized (e.g as document, collection, and store).
 * This enum identify a resources collection based on a consistent naming convention.
 * A collection resource is a server-managed directory of resources using a "plural" name to denote its type.
 */
public enum CollectionResourceArchetype {
    /**
     * Collections of organizations (e.g companies).
     */
    ORGANIZATIONS("organizations");

    private final String label;

    CollectionResourceArchetype(String label) {
        this.label = label;
    }

    /**
     * Get the collection name.
     *
     * @return A resource label.
     */
    public String label() {
        return this.label;
    }
}
