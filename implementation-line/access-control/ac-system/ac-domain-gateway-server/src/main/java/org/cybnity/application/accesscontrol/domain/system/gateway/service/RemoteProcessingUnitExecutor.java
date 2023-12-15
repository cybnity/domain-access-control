package org.cybnity.application.accesscontrol.domain.system.gateway.service;

import org.cybnity.application.accesscontrol.domain.system.gateway.routing.UISRecipientList;
import org.cybnity.application.accesscontrol.ui.api.UICapabilityChannel;
import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.IDescribed;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.Stream;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Processor ensuring a transfer of event to process (as a Proxy delegate) the treatment relative to an event.
 * For example, can be responsible to forward an event to a remote UI capability over a middleware stream.
 */
public class RemoteProcessingUnitExecutor implements ProcessingUnitDelegation {

    @Override
    public void process(IDescribed factEvent) throws IllegalArgumentException {
        if (factEvent==null) throw new IllegalArgumentException("factEvent parameter is required!");
        // Identify mapping key about supported event type name
        Attribute factType = factEvent.type();
        if (factType!=null) {
            String eventTypeName = factEvent.type().value();
            // Identify existing path (e.g UIS stream recipient) to the remote service component which is responsible of event treatment
            // based on RecipientList pattern implementation according to the fact event type name
            UISRecipientList destinationMap = new UISRecipientList();
            UICapabilityChannel PUEntrypointChannel = destinationMap.recipient(eventTypeName);
            if (PUEntrypointChannel!=null) {
                Stream domainEndpoint = new Stream(/* Detected capability domain path based on entrypoint supported fact event type */ destinationMap.recipient(eventTypeName).shortName());

                // TODO implement creation of a Proxy element that execute a forwarding action to an external (e.g UI capability feature executed in standalone service) responsible of event treatment

            } else {
                // None remote processing unit is defined as able to perform the event treatment
            }
        } else {
            // Impossible to identify the processing unit from undefined/unknown event type
            // Create log about conformity violation
            // TODO
        }
    }

    /**
     * Get the list of event types that are defined as to be treated according to this delegate.
     *
     * @return A collection of event type names, or empty list.
     */
    public static Collection<String> supportedEventNames() {
        Collection<String> managed = new ArrayList<>();

        // Set all the fact event type names of the referential
        // managed.add(CommandName.XXX.name());

        return managed;
    }
}
