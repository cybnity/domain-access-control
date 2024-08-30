package org.cybnity.application.accesscontrol.translator.ui.api;

import io.lettuce.core.StreamMessage;
import org.cybnity.application.accesscontrol.translator.ui.api.event.DomainEventType;
import org.cybnity.framework.domain.IPresenceObservability;
import org.cybnity.framework.domain.event.IEventType;
import org.cybnity.framework.domain.event.ProcessingUnitPresenceAnnounced;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.ICapabilityChannel;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.MessageMapper;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.NamingConventions;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.event.ProcessingUnitPresenceAnnouncedEventFactory;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.MessageMapperFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test validating the transformation capability provided by a mapper.
 */
public class JSONToPUPresenceAnnouncedTransformUseCaseTest {

    /**
     * Test a transformation between JSON string and ProcessingUnitPresenceAnnounced event type.
     */
    @Test
    public void givenStringMessage_whenTransformToValidEventType_thenEventTypeReturned() throws Exception {
        // Prepare sample of PU presence announce regarding entrypoint supported
        Map<IEventType, ICapabilityChannel> supportedEventTypesToRoutingPath = new HashMap<>();

        // Defining one or several routing paths as entrypoint
        supportedEventTypesToRoutingPath.put(DomainEventType.TENANT_REGISTRATION_SUBMITTED, UICapabilityChannel.access_control_tenant_registration);
        // Define a sample name of announcer
        String puServiceName = "OrganizationRegistrationFeature" + NamingConventions.SPACE_ACTOR_NAME_SEPARATOR + "capability";

        // Generate domain event sample
        ProcessingUnitPresenceAnnouncedEventFactory evtFactory = new ProcessingUnitPresenceAnnouncedEventFactory();
        ProcessingUnitPresenceAnnounced evt = evtFactory.create(supportedEventTypesToRoutingPath, puServiceName, null, IPresenceObservability.PresenceState.AVAILABLE);

        // --- STREAM MESSAGE TRANSFORMATION SCENARIO ---
        // Create its JSON version simulating a string version which could be received from a UIS channel (sharing as String message)
        ACDomainMessageMapperFactory mapperFactory = new ACDomainMessageMapperFactory();
        MessageMapper mapper = mapperFactory.getMapper(ProcessingUnitPresenceAnnounced.class, String.class);
        mapper.transform(evt);
        String JSONVersion = (String) mapper.getResult();
        Assertions.assertNotNull(JSONVersion);

        // Attempt to transform the JSON version to custom domain event type
        MessageMapper mapToCustomEventType = mapperFactory.getMapper(String.class, ProcessingUnitPresenceAnnounced.class);
        mapToCustomEventType.transform(JSONVersion);
        ProcessingUnitPresenceAnnounced retrievedEvt = (ProcessingUnitPresenceAnnounced) mapToCustomEventType.getResult();
        // Check transformation result
        Assertions.assertNotNull(retrievedEvt, "Shall be supported by custom mapping scenario!");
        // Check equal object
        Assertions.assertEquals(evt, retrievedEvt, "Shall be same content and identified!");
    }

    /**
     * Test a transformation between ProcessingUnitPresenceAnnounced event type and StreamMessage.
     */
    @Test
    public void givenStreamMessage_whenTransformToValidEventType_thenEventTypeReturned() throws Exception {
        // Prepare sample of PU presence announce regarding entrypoint supported
        Map<IEventType, ICapabilityChannel> supportedEventTypesToRoutingPath = new HashMap<>();

        // Defining one or several routing paths as entrypoint
        supportedEventTypesToRoutingPath.put(DomainEventType.TENANT_REGISTRATION_SUBMITTED, UICapabilityChannel.access_control_tenant_registration);
        // Define a sample name of announcer
        String puServiceName = "OrganizationRegistrationFeature" + NamingConventions.SPACE_ACTOR_NAME_SEPARATOR + "capability";

        // Generate domain event sample
        ProcessingUnitPresenceAnnouncedEventFactory evtFactory = new ProcessingUnitPresenceAnnouncedEventFactory();
        ProcessingUnitPresenceAnnounced evt = evtFactory.create(supportedEventTypesToRoutingPath, puServiceName, null, IPresenceObservability.PresenceState.AVAILABLE);
        Assertions.assertNotNull(evt);

        // --- STREAM MESSAGE TRANSFORMATION SCENARIO ---
        // Create its StreamMessage version simulating a string version which could be received from a UIS stream (sharing as StreamMessage)
        ACDomainMessageMapperFactory mapperFactory = new ACDomainMessageMapperFactory();
        MessageMapper mapper = new MessageMapperFactory().getMapper(ProcessingUnitPresenceAnnounced.class, StreamMessage.class);
        mapper.transform(evt);
        // Get a simulated stream message container (that does not contain identifier and specification elements)
        Map<String, String> streamVersion = (Map<String, String>) mapper.getResult();
        Assertions.assertNotNull(streamVersion);

        // Attempt to transform the StreamMessage version to custom domain event type
        MessageMapper mapToCustomEventType = mapperFactory.getMapper(StreamMessage.class, ProcessingUnitPresenceAnnounced.class);
        mapToCustomEventType.transform(streamVersion);
        ProcessingUnitPresenceAnnounced retrievedEvt = (ProcessingUnitPresenceAnnounced) mapToCustomEventType.getResult();
        // Check transformation result
        Assertions.assertNotNull(retrievedEvt, "Shall be supported by custom mapping scenario!");
        // Check equal object
        Assertions.assertEquals(evt, retrievedEvt, "Shall be same content and identified!");
    }
}

