package org.cybnity.application.accesscontrol.translator.ui.api;

import io.lettuce.core.StreamMessage;
import org.cybnity.application.accesscontrol.translator.ui.api.mapper.JSONMessageToProcessingUnitPresenceAnnouncedTransformer;
import org.cybnity.application.accesscontrol.translator.ui.api.mapper.StreamMessageToProcessingUnitPresenceAnnouncedTransformer;
import org.cybnity.framework.domain.event.ProcessingUnitPresenceAnnounced;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.MessageMapper;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.MessageMapperFactory;

/**
 * Utility class allowing to transform an object manageable by the Access Control domain according to a type of data structure supported by the domain's UI layer.
 * For example, translate a specific or generic CommandEvent object into a String (message body).
 */
public class ACDomainMessageMapperFactory extends MessageMapperFactory {

    public ACDomainMessageMapperFactory() {
        super();
    }

    /**
     * Get an object mapper allowing transformation of a domain specific type of class, or common type supported by the Users Interactions Space.
     *
     * @param transformable   Origin object type to map.
     * @param transformableAs Targeted type to generate.
     * @return A mapper, or null when none supported mapping capability between the origin and targeted type.
     */
    public MessageMapper getMapper(Class<?> transformable, Class<?> transformableAs) {
        if (transformable != null && transformableAs != null) {
            // Select the origin type to be transformed that is only specific to the AC domain
            if (StreamMessage.class.isAssignableFrom(transformable)) {
                // Select the mapper allowing transformation to targeted type
                if (ProcessingUnitPresenceAnnounced.class.isAssignableFrom(transformableAs)) {
                    return new StreamMessageToProcessingUnitPresenceAnnouncedTransformer();
                }
            } else if (String.class.isAssignableFrom(transformable)) {
                // Select the mapper allowing transformation to targeted type
                if (ProcessingUnitPresenceAnnounced.class.isAssignableFrom(transformableAs)) {
                    return new JSONMessageToProcessingUnitPresenceAnnouncedTransformer();
                }
            }
            // Else try to find existing common mapper supported by the UIS space
            return super.getMapper(transformable, transformableAs);
        }
        return null; // None supported types
    }
}
