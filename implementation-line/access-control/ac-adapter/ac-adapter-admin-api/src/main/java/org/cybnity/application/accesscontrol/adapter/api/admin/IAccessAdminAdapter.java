package org.cybnity.application.accesscontrol.adapter.api.admin;

import org.cybnity.framework.domain.ICleanup;
import org.cybnity.framework.domain.IHealthControl;

/**
 * Contract relative to access capabilities administration (e.g setting of system's client scopes, access control configuration supervision).
 * For example, services allowing realms, access workflows, standardized roles management according to privileged capabilities.
 */
public interface IAccessAdminAdapter extends ICleanup, IHealthControl {
}
