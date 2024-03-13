package org.cybnity.application.accesscontrol.ui.api.experience;

/**
 * Referential catalog of UI API store paths exposed for capabilities execution.
 * According to the best practices to use nouns (equals to thing instead of referring to an action based on verb) to represent resources, each resource archetype is categorized (e.g as document, collection, and store).
 * This enum identify a resources store based on a consistent naming convention.
 * A store is a client-managed resource repository. It lets an API client pur resources in, get them back out, and decide when to delete them.
 * A store never generates new URIs. Instead, each stored resource has a URI.
 * The URI was chosen by a client when the resource was initially put into the store.
 * Use "plural" name to denote store resource archetype.
 */
public enum StoreResourceArchetype {
    ;

    private final String label;

    StoreResourceArchetype(String label) {
        this.label = label;
    }

    /**
     * Get the store name.
     *
     * @return A resource label.
     */
    public String label() {
        return this.label;
    }
}
