package org.cybnity.accesscontrol.iam.domain.event;

import org.cybnity.framework.domain.event.IEventType;

/**
 * Type of API event supported by the IAM data model.
 * Each event name shall be an adjective representing a fact state.
 */
public enum DomainEventType implements IEventType {

    /**
     * Event about an account domain object created into the domain layer.
     */
    ACCOUNT_CREATED,

    /**
     * Event about an existing account upgraded into the domain layer.
     */
    ACCOUNT_CHANGED,

    /**
     * Event about an account deleted from the domain layer.
     */
    ACCOUNT_DELETED,

    /**
     * Event about a smart system domain object created into the domain layer.
     */
    SMART_SYSTEM_CREATED,

    /**
     * Event about an existing smart system upgraded into the domain layer.
     */
    SMART_SYSTEM_CHANGED,

    /**
     * Event about a smart system deleted from the domain layer.
     */
    SMART_SYSTEM_DELETED,

    /**
     * Event about an organizational structure domain object created into the domain layer.
     */
    ORGANIZATIONAL_STRUCTURE_CREATED,

    /**
     * Event about an existing organizational structure upgraded into the domain layer.
     */
    ORGANIZATIONAL_STRUCTURE_CHANGED,

    /**
     * Event about an organizational structure deleted from the domain layer.
     */
    ORGANIZATIONAL_STRUCTURE_DELETED,

    /**
     * Event about a person domain object created into the domain layer.
     */
    PERSON_CREATED,

    /**
     * Event about an existing person upgraded into the domain layer.
     */
    PERSON_CHANGED,

    /**
     * Event about a person deleted from the domain layer.
     */
    PERSON_DELETED

}
