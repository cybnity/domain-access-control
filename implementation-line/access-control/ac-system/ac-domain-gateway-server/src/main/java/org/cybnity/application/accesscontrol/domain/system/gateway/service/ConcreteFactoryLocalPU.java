package org.cybnity.application.accesscontrol.domain.system.gateway.service;

/**
 * Concrete factory of local processing unit (e.g embedded UI capability processing unit).
 */
public class ConcreteFactoryLocalPU extends ProcessingUnitDelegationFactory {
    public ConcreteFactoryLocalPU() {
        super();
    }

    @Override
    public ProcessingUnitDelegation createDelegate() {
        return new LocalProcessingUnitExecutor();
    }
}
