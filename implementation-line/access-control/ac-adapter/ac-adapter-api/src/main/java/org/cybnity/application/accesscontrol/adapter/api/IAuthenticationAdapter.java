package org.cybnity.application.accesscontrol.adapter.api;

import org.cybnity.framework.domain.ICleanup;
import org.cybnity.framework.domain.IHealthControl;

/**
 * Contract relative to Authentication and Authorization (UAM) of a system or a user into a context.
 * For example, provided services allowing connection of a user to be identified with reception of principal, permissions, roles, token...
 */
public interface IAuthenticationAdapter extends ICleanup, IHealthControl {
}
