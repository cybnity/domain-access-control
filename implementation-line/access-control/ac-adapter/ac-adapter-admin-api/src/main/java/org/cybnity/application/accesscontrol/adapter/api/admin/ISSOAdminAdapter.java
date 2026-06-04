package org.cybnity.application.accesscontrol.adapter.api.admin;

import org.cybnity.framework.domain.ICleanup;
import org.cybnity.framework.domain.IHealthControl;
import org.cybnity.framework.domain.model.Tenant;

/**
 * Contract regarding administration of Single-Sign On (SSO) capabilities that support users or systems on the User Identity and Access Management (UIAM).
 * These are administration and supervision features regarding UIAM solution.
 */
public interface ISSOAdminAdapter extends ICleanup, IHealthControl {
    /**
     * Create a new tenant identifiable by a logical name.
     *
     * @param tenantLabel Mandatory defined label allowing unique identification name of the tenant to create.
     * @return New created tenant including description.
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     */
    public Tenant createTenant(String tenantLabel) throws IllegalArgumentException;
}
