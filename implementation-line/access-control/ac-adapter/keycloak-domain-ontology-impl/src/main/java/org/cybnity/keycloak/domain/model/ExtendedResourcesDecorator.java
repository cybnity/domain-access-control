package org.cybnity.keycloak.domain.model;

/**
 * Decorator pattern allowing customization of a resource (e.g; Keycloak component) with additional resources (e.g; client scope, realm roles, realm settings, extended configuration) as dynamics additional responsibilities.
 * This configuration component allow to reuse common configuration to any Keycloak resource.
 */
public interface ExtendedResourcesDecorator {

    /**
     * Apply a decoration of this object with additional customization elements.
     * This method is responsible to create the customization rules (e.g; instantiation of additional customization elements to this object; or to change some current object's attributes) and shall be implemented by any subclass as a concrete decorator pattern implementation.
     */
    public void decorate();
}
