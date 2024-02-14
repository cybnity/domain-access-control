package org.cybnity.application.accesscontrol.ui.api.event;

/**
 * Type of standard attribute supported by the AC domain events and command.
 * Referential that can be used to identify a type of specification attribute with a value.
 */
public enum AttributeName {

    /**
     * Logical name of a service provider (e.g domain feature notifying event)
     */
    SERVICE_NAME,

    /**
     * Type of processing result cause.
     */
    OUTPUT_CAUSE_TYPE;
}
