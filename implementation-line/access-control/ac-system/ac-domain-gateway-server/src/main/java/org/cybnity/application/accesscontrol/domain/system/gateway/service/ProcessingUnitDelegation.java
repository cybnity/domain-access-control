package org.cybnity.application.accesscontrol.domain.system.gateway.service;

import org.cybnity.framework.domain.IDescribed;

/**
 * Delegation management  to a processing unit ensuring the realization of a work (e.g fact event treatment).
 */
public interface ProcessingUnitDelegation {

    /**
     * Execute the delegation process according to the specific mode (embedded or remote processing) of execution supported by this delegate.
     *
     * @param factEvent Mandatory event to process.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    void process(IDescribed factEvent) throws IllegalArgumentException;

}
