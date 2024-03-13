package org.cybnity.accesscontrol.iam.domain.model;

import org.cybnity.accesscontrol.iam.domain.event.DomainEventType;
import org.cybnity.framework.IContext;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.DomainEvent;
import org.cybnity.framework.domain.event.ConcreteDomainChangeEvent;
import org.cybnity.framework.domain.event.HydrationAttributeProvider;
import org.cybnity.framework.domain.event.IAttribute;
import org.cybnity.framework.domain.model.DomainEntity;
import org.cybnity.framework.domain.model.SocialEntity;
import org.cybnity.framework.immutable.Entity;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.framework.immutable.ImmutabilityException;
import org.cybnity.framework.immutable.utility.VersionConcreteStrategy;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Represent a software and/or hardware system (e.g autonomous accessory
 * representing a person or organization) who can have interactions with
 * systems.
 *
 * @author olivier
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_3")
public class SmartSystem extends SocialEntity {

    /**
     * Version of this class type.
     */
    private static final long serialVersionUID = new VersionConcreteStrategy()
            .composeCanonicalVersionHash(SmartSystem.class).hashCode();

    /**
     * Attribute type managed via command event allowing change of this aggregate, and/or allowing notification of information changed via a promoted event type.
     */
    public enum Attribute implements IAttribute {
        ;
    }

    /**
     * Default constructor.
     * During the construction, a SMART_SYSTEM_CREATED domain event is automatically added to the lifecycle changes history container.
     *
     * @param predecessor Mandatory parent of this child entity.
     * @param id          Unique and optional identifier of this instance.
     * @throws IllegalArgumentException When predecessor mandatory parameter is not
     *                                  defined or without defined identifier.
     */
    public SmartSystem(Entity predecessor, Identifier id) throws IllegalArgumentException {
        super(predecessor, id);
        try {
            // Add a change event into the history
            ConcreteDomainChangeEvent changeEvt = prepareChangeEventInstance(DomainEventType.SMART_SYSTEM_CREATED);

            // Add to changes history
            addChangeEvent(changeEvt);
        } catch (ImmutabilityException ie) {
            // Log potential coding problem relative to immutability support
            logger().log(Level.SEVERE, ie.getMessage(), ie);
        }
    }

    /**
     * Default constructor.
     * During the construction, a SMART_SYSTEM_CREATED domain event is automatically added to the lifecycle changes history container.
     *
     * @param predecessor Mandatory parent of this child entity.
     * @param identifiers Set of optional base identifiers of this instance, that
     *                    contains non-duplicable elements.
     * @throws IllegalArgumentException When identifiers parameter is null or each
     *                                  item does not include name and value.
     */
    public SmartSystem(Entity predecessor, LinkedHashSet<Identifier> identifiers) throws IllegalArgumentException {
        super(predecessor, identifiers);
        try {
            // Add a change event into the history
            ConcreteDomainChangeEvent changeEvt = prepareChangeEventInstance(DomainEventType.SMART_SYSTEM_CREATED);

            // Add to changes history
            addChangeEvent(changeEvt);
        } catch (ImmutabilityException ie) {
            // Log potential coding problem relative to immutability support
            logger().log(Level.SEVERE, ie.getMessage(), ie);
        }
    }

    /**
     * Factory of instance from historized facts (e.g fact creation, change, deletion events) allowing the instance rehydration.
     *
     * @param instanceId     Mandatory unique identifier of the child fact instance to rehydrate.
     * @param changesHistory Mandatory not empty history. History order shall be ascending ordered with the last list element equals to the more young creation event relative to this instance to rehydrate.
     * @throws IllegalArgumentException When mandatory parameter is not valid or empty. When list does not contain identifiable creation event as first list element.
     */
    public static SmartSystem instanceOf(Identifier instanceId, List<DomainEvent> changesHistory) throws IllegalArgumentException {
        if (instanceId == null) throw new IllegalArgumentException("instanceId parameter is required!");
        if (changesHistory == null || changesHistory.isEmpty())
            throw new IllegalArgumentException("changesHistory parameter is required and shall be not empty!");

        // Get first element as origin creation event (more old event)
        DomainEvent event = changesHistory.get(0);
        if (event == null) throw new IllegalArgumentException("First history item shall be not null!");

        // Normally, any event relative to a smart system change shall include specification attributes allowing its instantiation
        if (HydrationAttributeProvider.class.isAssignableFrom(event.getClass())) {
            HydrationAttributeProvider hydrationElementsProvider = (HydrationAttributeProvider) event;
            Identifier tenantId = hydrationElementsProvider.changeSourceIdentifier();
            OffsetDateTime occurredAt = hydrationElementsProvider.changeSourceOccurredAt();
            Identifier parentId = hydrationElementsProvider.changeSourcePredecessorReferenceId();

            // Read identification of smart system mandatory predecessor entity reference
            if (parentId != null && tenantId != null && occurredAt != null) {
                DomainEntity parent = new DomainEntity(parentId);
                parent.setCreatedAt(occurredAt); /* re-hydrate origin creation date */

                // Re-instantiate the fact
                SmartSystem fact = new SmartSystem(parent, tenantId /* re-hydrated domain data identity privileged from event attributes*/);
                fact.setOccurredAt(occurredAt);

                // Quality control of normally equals re-hydrated identity of data object
                if (!fact.identified().equals(instanceId)) {
                    // Problem of integrity regarding the re-hydrated identification elements from the event
                    throw new IllegalArgumentException("Non conformity of the identifiers detected into the change event history which shall be equals to the instanceId parameter requested to be re-hydrated!");
                }

                // Rehydrate its status for events history into the last known state (without lifecycle history storage by the instance)
                fact.mutate(changesHistory);
                // Clean the re-hydrated change events potential automatically added during mutation operations
                fact.changeEvents().clear();

                return fact; // Return re-hydrated instance
            }
        }

        throw new IllegalArgumentException("Impossible re-hydration of smart system instance from changes history!");
    }

    /**
     * Specific and redefined implementation of change re-hydration.
     *
     * @param change Mandatory change to apply on subject according to the change type (e.g attribute add, upgrade, delete operation).
     * @throws IllegalArgumentException When missing required parameter.
     */
    @Override
    public void mutateWhen(DomainEvent change) throws IllegalArgumentException {
        super.mutateWhen(change);// Execute potential re-hydration of super class
        // Apply local change without feeding of lifecycle history modification
        // Only about change event (DomainEventType.SMART_SYSTEM_CREATED or DomainEventType.SMART_SYSTEM_DELETED not managed as mutation to apply)
        // if (DomainEventType.SMART_SYSTEM_CHANGED.name().equals(change.type().value())) {

        //}
    }

    /**
     * Implement the generation of version hash regarding this class type.
     */
    @Override
    public String versionHash() {
        return String.valueOf(serialVersionUID);
    }

    /**
     * Get the serial version UID of this class type.
     * @return A serial version UID.
     */
    public static long serialVersionUID() {
        return serialVersionUID;
    }

    @Override
    public Serializable immutable() throws ImmutabilityException {
        LinkedHashSet<Identifier> ids = new LinkedHashSet<>(this.identifiers());
        SmartSystem smartSystem = new SmartSystem(parent(), ids);
        smartSystem.occurredAt = this.occurredAt();
        return smartSystem;
    }

    @Override
    public void handle(Command command, IContext iContext) throws IllegalArgumentException {
    }

    @Override
    public Set<String> handledCommandTypeVersions() {
        return new LinkedHashSet<>(); // None type of Command is currently handled by this object
    }
}
