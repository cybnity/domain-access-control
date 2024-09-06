package org.cybnity.application.accesscontrol.domain.system.gateway.service;

import org.cybnity.application.accesscontrol.domain.system.gateway.CustomContextualizedTest;
import org.cybnity.application.accesscontrol.translator.ui.api.UICapabilityChannel;
import org.cybnity.application.accesscontrol.ui.api.event.CommandName;
import org.cybnity.application.accesscontrol.ui.api.event.TenantRegistrationAttributeName;
import org.cybnity.framework.application.vertx.common.routing.IEventProcessingManager;
import org.cybnity.framework.application.vertx.common.routing.RouteRecipientList;
import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.event.CommandFactory;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Test of behaviors regarding the detection and filtering of supported fact event by IO API.
 *
 * @author olivier
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class APISupportedCapabilitySelectionFilterUseCaseTest extends CustomContextualizedTest {

    /**
     * Default constructor.
     */
    public APISupportedCapabilitySelectionFilterUseCaseTest() {
        super(false, false, false, false, /* With snapshots management capability activated */ false);
    }
    /**
     * Sample of supported event types equals to implementation class normally supportable scope of commands.
     */
    private static final IEventProcessingManager supportableEventTypesProvider = new IEventProcessingManager() {
        @Override
        public RouteRecipientList delegateDestinations() {
            RouteRecipientList list = new RouteRecipientList();
            // Define all supported event types by the filter
            // See TenantRegistrationFeaturePipeline of RTS computation unit project supportedEventTypesToRoutingPath() method for examples
            list.addRoute(CommandName.REGISTER_TENANT.name(), UICapabilityChannel.access_control_tenant_registration.shortName());
            return list;
        }
    };

    /**
     * Test detection and reject of undefined fact event type by the filtering process.
     */
    @Test
    void givenEventsFilterConfiguration_whenHandlingNullFactEvent_thenSelectionRejected() {
        // Create a filter processing unit
        APISupportedCapabilitySelectionFilter filter = new APISupportedCapabilitySelectionFilter(new Stream(UICapabilityChannel.access_control_in.shortName()), supportableEventTypesProvider);
        // Execute the filter process
        Assertions.assertFalse(filter.process(null));
    }

    /**
     * Test detection and reject of unknown fact event type by the filtering process.
     */
    @Test
    void givenEventsFilterConfiguration_whenHandlingUnsupportedFactEvent_thenSelectionRejected() {
        // Create a filter processing unit
        APISupportedCapabilitySelectionFilter filter = new APISupportedCapabilitySelectionFilter(new Stream(UICapabilityChannel.access_control_in.shortName()), supportableEventTypesProvider);
        // Create an unknown fact sample (e.g other domain fact or security attack data entry) that should not be supported by the filter
        Collection<Attribute> definition = new ArrayList<>();
        // Set organization name
        Attribute tenantNameToRegister = new Attribute(TenantRegistrationAttributeName.TENANT_NAMING.name(), "CYBNITY");
        definition.add(tenantNameToRegister);
        // Prepare unknown command event to perform via API
        Command requestEvent = CommandFactory.create("UNKNOWN_EVENT_TYPE", null, definition, null, null);
        // Auto-assign correlation identifier allowing finalized transaction check
        requestEvent.generateCorrelationId(null);
        // Execute the filter process
        Assertions.assertFalse(filter.process(requestEvent));
    }

    /**
     * Test detection and selection of supported fact event types by the filtering process.
     */
    @Test
    void givenEventsFilterConfiguration_whenHandlingSupportedFactEvent_thenSelectionConfirmed() {
        // Create a filter processing unit
        APISupportedCapabilitySelectionFilter filter = new APISupportedCapabilitySelectionFilter(new Stream(UICapabilityChannel.access_control_in.shortName()), supportableEventTypesProvider);
        // Create valid fact sample that should be supported by the filter
        // Prepare RegisterOrganization command event including organization naming
        Collection<Attribute> definition = new ArrayList<>();
        // Set organization name
        Attribute tenantNameToRegister = new Attribute(TenantRegistrationAttributeName.TENANT_NAMING.name(), "CYBNITY");
        definition.add(tenantNameToRegister);
        // Prepare RegisterOrganization command event to perform via API
        Command requestEvent = CommandFactory.create(CommandName.REGISTER_TENANT.name(),
                /* No identified as anonymous transaction without correlation id need*/ null, definition,
                /* none prior command to reference*/ null,
                /* None pre-identified organization because new creation */ null);
        // Auto-assign correlation identifier allowing finalized transaction check
        requestEvent.generateCorrelationId(null);

        // Execute the filter process
        Assertions.assertTrue(filter.process(requestEvent));
    }

}
