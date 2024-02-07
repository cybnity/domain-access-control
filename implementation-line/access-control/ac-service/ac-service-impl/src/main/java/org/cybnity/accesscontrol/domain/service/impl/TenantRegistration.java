package org.cybnity.accesscontrol.domain.service.impl;

import org.cybnity.accesscontrol.domain.service.ITenantRegistrationService;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.application.ApplicationService;

/**
 * Application service implementation component.
 */
public class TenantRegistration extends ApplicationService implements ITenantRegistrationService {

    @Override
    public void handle(Command command) throws IllegalArgumentException {

    }
}
