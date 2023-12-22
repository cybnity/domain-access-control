package org.cybnity.application.accesscontrol.ui.api.experience;

/**
 * Referential catalog of UI API processing unit paths exposed for capabilities execution management.
 * According to the best practices to use nouns (equals to thing instead of referring to an action based on verb) to represent resources, each resource archetype is categorized (e.g as document, collection, and store).
 * This enum identify a resources executor based on a consistent naming convention.
 * Use "plural" name to denote store resource archetype.
 */
public enum ExecutionResource {

    /**
     * Represent a processing unit (e.g UI capability processor) able to manage a treatment (e.g a capability event request processing).
     */
    PROCESSING_UNIT("processing_unit"),

    /**
     * Represent a gateway of services (e.g Domain IO gateway) able to manage interactions between multiple sides' components.
     */
    GATEWAY("gateway");

    private final String label;

    ExecutionResource(String label) {
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
