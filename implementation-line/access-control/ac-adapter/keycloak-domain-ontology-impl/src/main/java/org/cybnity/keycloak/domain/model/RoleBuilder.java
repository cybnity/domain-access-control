package org.cybnity.keycloak.domain.model;

/**
 * Builder pattern implementation class allowing to respect the build rules of Keycloak regarding a RoleRepresentation object.
 * This class check the authorized values eligible for RoleRepresantation build according to the format rules supported by Keycloak.
 * See specification of value supported at <a href="https://www.keycloak.org/docs-api/latest/rest-api/index.html#RoleRepresentation">RoleRepresentation object via JSON</a>.
 *
 * @author olivier
 */
public class RoleBuilder {

    /**
     * Attribute key name usable on a role to define when it can be associated by default with clients.
     */
    public static String ATTRIBUTE_ASSOCIABLE_WITH_CLIENTS_BY_DEFAULT_KEY = "ASSOCIABLE_WITH_CLIENTS_BY_DEFAULT_KEY";

    String name;
    String description;
    Boolean isClientRole = Boolean.FALSE;
    Boolean isComposite = Boolean.FALSE;

    /**
     * Default constructor.
     */
    public RoleBuilder() {
    }

    /**
     * Prepare and return an instance of Role.
     *
     * @return A realm object including default configuration settings defined.
     */
    public Role build() {
        return new Role(this);
    }

    /**
     * The name of the Role.
     * This method only apply basic Keycloak minimum sanitization rule that check is real name is not empty and does not contain space character.
     * To ensure better sanitization of role name, use {@link Sanitizer#removeAllBlankCharacters(String)} method before to clean the role name about multiple special characters generating potential problem during its usage.
     *
     * @param roleName A mandatory defined logical name.
     * @return This builder instance.
     * @throws IllegalArgumentException When roleName parameter value is empty or does not respect format rule (e.g; not empty, not blank character).
     */
    public RoleBuilder name(String roleName) throws IllegalArgumentException {
        if (roleName == null)
            throw new IllegalArgumentException("The name parameter is required!");

        // Check supported value and authorized formatting rules
        if (roleName.isEmpty()) // Not empty
            throw new IllegalArgumentException("The name value shall not be empty!");

        if (roleName.contains(" ")) // Not blank character
            throw new IllegalArgumentException("The name value shall not contain spaces!");

        this.name = roleName;
        return this;
    }

    /**
     * The confirmation of role composite state.
     *
     * @param isComposite True if the role is a composite.
     * @return This builder instance.
     */
    public RoleBuilder isComposite(Boolean isComposite) {
        this.isComposite = isComposite;
        return this;
    }

    /**
     * The confirmation of role scope as defined for a client scope.
     *
     * @param isClientRole True if the role is dedicated to scope of a client.
     * @return This builder instance.
     */
    public RoleBuilder isClientRole(Boolean isClientRole) {
        this.isClientRole = isClientRole;
        return this;
    }

    /**
     * The description of the role.
     *
     * @param description A textual description helping to understand what goal, usage, scope is concerned by the role.
     * @return This builder instance.
     */
    public RoleBuilder description(String description) {
        this.description = description;
        return this;
    }

}
