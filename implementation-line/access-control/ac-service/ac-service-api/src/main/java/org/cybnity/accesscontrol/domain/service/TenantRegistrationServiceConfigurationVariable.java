package org.cybnity.accesscontrol.domain.service;

import org.cybnity.framework.IReadableConfiguration;

/**
 * Tenant registration service settings supporting dynamic and specific behavior change of runtime based on environment variables state.
 */
public enum TenantRegistrationServiceConfigurationVariable implements IReadableConfiguration {

    /**
     * True or False boolean value supported.
     */
    TENANT_REGISTRATION_AUTHORIZED_REASSIGNMENT("TENANT_REGISTRATION_AUTHORIZED_REASSIGNMENT");

    private final String name;

    private TenantRegistrationServiceConfigurationVariable(String aName) throws IllegalArgumentException {
        if (aName != null && !"".equalsIgnoreCase(aName)) {
            this.name = aName;
        } else {
            throw new IllegalArgumentException("The name of this variable shall be defined!");
        }
    }

    public String getName() {
        return this.name;
    }
}
