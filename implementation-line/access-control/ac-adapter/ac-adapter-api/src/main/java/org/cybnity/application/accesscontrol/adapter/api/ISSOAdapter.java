package org.cybnity.application.accesscontrol.adapter.api;

import org.cybnity.framework.domain.ICleanup;
import org.cybnity.framework.domain.IHealthControl;

/**
 * Contract regarding Single-Sign On (SSO) capabilities that support users or systems on the User Identity and Access Management (UIAM).
 * These are common features regarding UIAM usage in an SSO approach and provide services required to participate into an SSO process.
 */
public interface ISSOAdapter extends ICleanup, IHealthControl {

}
