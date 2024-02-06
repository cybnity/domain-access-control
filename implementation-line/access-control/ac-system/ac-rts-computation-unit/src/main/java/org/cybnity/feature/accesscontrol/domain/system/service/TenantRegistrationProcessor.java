package org.cybnity.feature.accesscontrol.domain.system.service;

import org.cybnity.framework.application.vertx.common.service.FactBaseHandler;
import org.cybnity.framework.domain.IDescribed;

/**
 * Execute the application feature as a pipelined way
 */
public class TenantRegistrationProcessor extends FactBaseHandler {

    @Override
    public boolean process(IDescribed iDescribed) {
        return false;
    }

    @Override
    protected boolean canHandle(IDescribed iDescribed) {
        return false;
    }
}
