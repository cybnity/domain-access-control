package org.cybnity.application.accesscontrol.adapter.api.admin;

import org.cybnity.application.accesscontrol.adapter.api.model.TenantDTO;
import org.cybnity.framework.domain.ICleanup;
import org.cybnity.framework.domain.IHealthControl;

/**
 * Contract relative to access capabilities administration (e.g setting of system's client scopes, access control configuration supervision).
 * For example, services allowing realms, access workflows, standardized roles management according to privileged capabilities.
 */
public interface IAccessAdminAdapter extends ICleanup, IHealthControl {

    /**
     * Create a new tenant identifiable by a logical name.
     *
     * @param tenantLabel Mandatory defined label allowing unique identification name of the tenant to create.
     * @return New created tenant including description.
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     * @throws OperationException       When the requested creation operation occurred a logical or technical problem.
     */
    public TenantDTO createTenant(String tenantLabel) throws IllegalArgumentException, OperationException;

    /**
     * Delete a tenant and all its configuration data.
     *
     * @param tenantLabel Mandatory tenant name to delete.
     * @param force       True if deletion of the real shall be forced even if important data depends on it (e.g; user accounts, technical clients configuration, configuration elements).
     * @return True if tenant with same name was existing and have been deleted. False if none deletion have been performed (e.g; unknown pre-existing tenant with same label; not forced deletion rule applied).
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     * @throws OperationException       When the requested deletion operation occurred a logical or technical problem.
     */
    public boolean deleteTenant(String tenantLabel, boolean force) throws IllegalArgumentException, OperationException;

    /**
     * Search existing tenant with equals name.
     *
     * @param tenantLabel Mandatory label to search.
     * @return Found tenant with equals name. Or null is none found.
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     * @throws OperationException       When the requested deletion operation occurred a logical or technical problem.
     */
    public TenantDTO findTenantByLabel(String tenantLabel) throws IllegalArgumentException, OperationException;
}
