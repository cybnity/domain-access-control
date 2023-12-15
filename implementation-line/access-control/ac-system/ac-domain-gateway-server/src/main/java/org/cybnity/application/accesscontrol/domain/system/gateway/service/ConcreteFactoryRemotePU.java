package org.cybnity.application.accesscontrol.domain.system.gateway.service;

/**
 * Concrete factory of remote processing unit (e.g proxy to UI capability processing unit reachable over UIS).
 */
public class ConcreteFactoryRemotePU extends ProcessingUnitDelegationFactory {
    public ConcreteFactoryRemotePU() {
        super();
    }

    @Override
    public ProcessingUnitDelegation createDelegate() {
        return new RemoteProcessingUnitExecutor();
    }
}
