package org.cybnity.feature.accesscontrol.domain.system.service;

import org.cybnity.framework.application.vertx.common.service.AbstractServiceActivator;
import org.cybnity.framework.domain.IDescribed;

/**
 * Tenant registration service activator implementing an embedded mode of TenantRegistration component called.
 */
public class TenantRegistrationActivator extends AbstractServiceActivator {

    @Override
    public boolean process(IDescribed fact) {
        // Execute the process rules on the received event
        return false;
    }

    @Override
    protected boolean canHandle(IDescribed fact) {

        return false;
    }

    @Override
    protected void moveToInvalidMessageChannel(IDescribed unprocessedEvent, String cause) {

    }

    @Override
    protected void moveToDeadLetterChannel(IDescribed unprocessedEvent, String cause) {

    }
}
