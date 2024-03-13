package org.cybnity.accesscontrol.domain.service.api;

/**
 * Type of application service output in terms of concerns and/or cause (e.g cause of rejection).
 */
public enum ApplicationServiceOutputCause {

    /**
     * A tenant is already existing, assigned with organization (e.g social entity with equals name).
     */
    EXISTING_TENANT_ALREADY_ASSIGNED;
}
