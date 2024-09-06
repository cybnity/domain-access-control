package org.cybnity.application.accesscontrol.adapter.api;

import org.cybnity.framework.domain.ICleanup;
import org.cybnity.framework.domain.IHealthControl;

/**
 * Contract relative to access information management (e.g personal account data, personal token) of a system or a user into a context.
 * For example, services allowing profile, permissions, roles, account management according to authorized capabilities.
 */
public interface IAccessManagementAdapter extends ICleanup, IHealthControl {
}
