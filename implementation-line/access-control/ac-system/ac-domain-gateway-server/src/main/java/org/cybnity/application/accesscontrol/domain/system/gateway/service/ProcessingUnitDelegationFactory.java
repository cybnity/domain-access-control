package org.cybnity.application.accesscontrol.domain.system.gateway.service;

import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.IDescribed;

/**
 * Factory design pattern implementation providing several categories of processing units according to a type of fact event to consume.
 * A processing unit can be:
 * - executable remotely (e.g Proxy PU delegating the event treatment over middleware stream)
 * - executable locally in embedded mode (e.g Thread PU performing the treatment as a local task)
 * The factory identify and select a PU factory that is identified as supporting an event type treatment.
 */
public abstract class ProcessingUnitDelegationFactory {

    /**
     * Create an instance of execution delegate.
     *
     * @return A delegation instance able to manage a fact event processing.
     */
    public abstract ProcessingUnitDelegation createDelegate();

    /**
     * Get a delegate factory able to provide a computation unit supporting an event type according to execution model configuration.
     *
     * @param factEvent Mandatory event type requiring to be processed.
     * @return A delegate factory. Return null when event type can not be managed by any processing unit factory.
     * @throws IllegalArgumentException When event parameter is null. When impossible identification of the event type.
     */
    static public ProcessingUnitDelegationFactory getInstance(IDescribed factEvent) throws IllegalArgumentException {
        if (factEvent == null) throw new IllegalArgumentException("FactEvent parameter is required!");
        // Identify the delegation supporting
        Attribute eventType = factEvent.type();
        if (eventType != null) {
            String eventTypeName = eventType.value();
            if (!eventTypeName.isEmpty()) {
                // Identify which processing unit executor type can be delegated as treatment responsible
                // Firstly evaluate if delegation can be privileged to a dedicated scalable and independent processing unit (cause: better performance distributed and optimized by deployment/configuration grid)
                if (RemoteProcessingUnitExecutor.supportedEventNames().contains(eventTypeName))
                    return new ConcreteFactoryRemotePU(); // Provide remote executor factory

                // Secondly search if a local execution can be managed
                if (LocalProcessingUnitExecutor.supportedEventNames().contains(eventTypeName))
                    return new ConcreteFactoryLocalPU(); // Provide local executor factory
            } else {
                // Impossible to provide a delegate about no identifiable
                // Scenario equals to a reject of event processing
                // So unsupported event type
                throw new IllegalArgumentException("Unknown event type name causing impossible processing unit factory providing!");
            }
        } else {
            throw new IllegalArgumentException("Unknown event type causing impossible processing unit providing!");
        }
        return null; // Confirm that none PU provider is able to manage the processing of the event type
    }
}
