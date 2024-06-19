package org.cybnity.accesscontrol.iam.domain.model;

import org.cybnity.accesscontrol.iam.domain.event.DomainEventType;
import org.cybnity.framework.IContext;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.DomainEvent;
import org.cybnity.framework.domain.IdentifierStringBased;
import org.cybnity.framework.domain.event.ConcreteDomainChangeEvent;
import org.cybnity.framework.domain.event.EventSpecification;
import org.cybnity.framework.domain.event.HydrationAttributeProvider;
import org.cybnity.framework.domain.event.IAttribute;
import org.cybnity.framework.domain.model.Aggregate;
import org.cybnity.framework.domain.model.DomainEntity;
import org.cybnity.framework.domain.model.Predecessors;
import org.cybnity.framework.immutable.*;
import org.cybnity.framework.immutable.utility.VersionConcreteStrategy;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Represent an allowed set of credentials (e.g cryptographic keys that enable
 * the subject to sign or encrypt data) to an owner (e.g organization's member),
 * according to a role (e.g user principal), with accorded privileges (e.g
 * regarding systems and/or capabilities) in the frame of a context (e.g
 * specific Tenant scope).
 * <p>
 * Domain root aggregate object relative to a subject's usable account.
 *
 * @author olivier
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_3")
public class Account extends Aggregate {

    /**
     * Owner entity (identifier) of this account.
     */
    private EntityReference owner;

    /**
     * Tenant entity (identifier) where this account can be used.
     */
    private EntityReference tenant;

    /**
     * Version of this class
     */
    private static final long serialVersionUID = new VersionConcreteStrategy().composeCanonicalVersionHash(Account.class).hashCode();

    /**
     * Attribute type managed via command event allowing change of this aggregate, and/or allowing notification of information changed via a promoted event type.
     */
    public enum Attribute implements IAttribute {
        /**
         * Identifier of the account owner reference.
         */
        OWNER_REFERENCE_ID,

        /**
         * Type of identifier that is supported as account owner reference.
         */
        OWNER_REFERENCE_IDENTIFIER_NAME,

        /**
         * Identifier of the tenant reference where this account is usable.
         */
        TENANT_REFERENCE_ID,

        /**
         * Type of identifier that is supported as tenant reference.
         */
        TENANT_REFERENCE_IDENTIFIER_NAME,
    }

    /**
     * Default constructor.
     * During the construction, an ACCOUNT_CREATED domain event is automatically added to the lifecycle changes history container.
     * Update event are also added into the change events history regarding assigned owner and tenant.
     *
     * @param predecessor          Mandatory parent of this tenant root aggregate
     *                             instance.
     * @param id                   Optional identifier of this user account.
     * @param accountOwnerIdentity Mandatory identity of owner (e.g user identity)
     *                             of this account.
     * @param tenantIdentity       Optional identity of the tenant (e.g organization
     *                             subscription that this account is registered
     *                             into) and usable.
     * @throws IllegalArgumentException When id parameter is null and does not
     *                                  include name and value. When id parameter is
     *                                  not based on BaseConstants.IDENTIFIER_ID
     *                                  name.
     */
    public Account(Entity predecessor, Identifier id, EntityReference accountOwnerIdentity, EntityReference tenantIdentity) throws IllegalArgumentException {
        this(predecessor, id);
        if (id != null && !BaseConstants.IDENTIFIER_ID.name().equals(id.name()))
            throw new IllegalArgumentException("id parameter is not valid because identifier name shall be equals to only supported value (" + BaseConstants.IDENTIFIER_ID.name() + ")!");
        if (accountOwnerIdentity == null)
            throw new IllegalArgumentException("accountOwnerIdentity parameter is required!");
        // Save unmodifiable user identity which is owner of this account
        this.setOwner(accountOwnerIdentity);

        if (tenantIdentity != null) {
            // Save the tenant identity which is a scope of usage regarding this account
            // (e.g attached to the subscription of the tenant)
            this.setTenant(tenantIdentity);
        }
    }

    /**
     * Specific partial constructor of an identifiable account.
     * During the construction, an ACCOUNT_CREATED domain event is automatically added to the lifecycle changes history container.
     *
     * @param predecessor Mandatory parent of this tenant root aggregate instance.
     * @param identifiers Optional set of identifiers of this entity, that contains
     *                    non-duplicable elements.
     * @throws IllegalArgumentException When identifiers parameter is null or each
     *                                  item does not include name and value.
     */
    private Account(Entity predecessor, LinkedHashSet<Identifier> identifiers) throws IllegalArgumentException {
        super(predecessor, identifiers);
        try {
            // Add a change event into the history
            ConcreteDomainChangeEvent changeEvt = prepareChangeEventInstance(DomainEventType.ACCOUNT_CREATED);

            // Add to changes history
            addChangeEvent(changeEvt);
        } catch (ImmutabilityException ie) {
            // Log potential coding problem relative to immutability support
            logger().log(Level.SEVERE, ie.getMessage(), ie);
        }
    }

    /**
     * Default constructor.
     * During the construction, an ACCOUNT_CREATED domain event is automatically added to the lifecycle changes history container.
     *
     * @param predecessor Mandatory parent of this tenant root aggregate entity.
     * @param id          Optional identifier of this tenant.
     * @throws IllegalArgumentException When any mandatory parameter is missing.
     *                                  When id parameter's name is not equals to
     *                                  BaseConstants.IDENTIFIER_ID. When a problem
     *                                  of immutability is occurred. When
     *                                  predecessor mandatory parameter is not
     *                                  defined or without defined identifier.
     */
    public Account(Entity predecessor, Identifier id) throws IllegalArgumentException {
        super(predecessor, id);
        try {
            // Add a change event into the history
            ConcreteDomainChangeEvent changeEvt = prepareChangeEventInstance(DomainEventType.ACCOUNT_CREATED);

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
     * @return Created account instance.
     * @throws IllegalArgumentException When mandatory parameter is not valid or empty. When list does not contain identifiable creation event as first list element.
     */
    public static Account instanceOf(Identifier instanceId, List<DomainEvent> changesHistory) throws IllegalArgumentException {
        if (instanceId == null) throw new IllegalArgumentException("instanceId parameter is required!");
        if (changesHistory == null || changesHistory.isEmpty())
            throw new IllegalArgumentException("changesHistory parameter is required and shall be not empty!");

        // Get first element as origin creation event (more old event)
        DomainEvent event = changesHistory.get(0);
        if (event == null) throw new IllegalArgumentException("First history item shall be not null!");

        // Normally, any event relative to an account change shall include specification attributes allowing its instantiation
        if (HydrationAttributeProvider.class.isAssignableFrom(event.getClass())) {
            HydrationAttributeProvider hydrationElementsProvider = (HydrationAttributeProvider) event;
            Identifier tenantId = hydrationElementsProvider.changeSourceIdentifier();
            OffsetDateTime occurredAt = hydrationElementsProvider.changeSourceOccurredAt();
            Identifier parentId = hydrationElementsProvider.changeSourcePredecessorReferenceId();

            // Read identification of account mandatory predecessor entity reference
            if (parentId != null && tenantId != null && occurredAt != null) {
                DomainEntity parent = new DomainEntity(parentId);
                parent.setCreatedAt(occurredAt); /* re-hydrate origin creation date */

                // Re-instantiate the fact
                Account fact = new Account(parent, tenantId /* re-hydrated domain data identity privileged from event attributes*/);
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

        throw new IllegalArgumentException("Impossible re-hydration of account instance from changes history!");
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
        // Only about change event (DomainEventType.ACCOUNT_CREATED or DomainEventType.ACCOUNT_DELETED not managed as mutation to apply)
        if (DomainEventType.ACCOUNT_CHANGED.name().equals(change.type().value())) {
            try {
                // Account which instance's attribute shall be rehydrated

                // --- OWNER REFERENCE MUTATION ---
                org.cybnity.framework.domain.Attribute ownerRefIdChanged = EventSpecification.findSpecificationByName(Attribute.OWNER_REFERENCE_ID.name(), change.specification());
                org.cybnity.framework.domain.Attribute ownerRefTypeChanged = EventSpecification.findSpecificationByName(Attribute.OWNER_REFERENCE_IDENTIFIER_NAME.name(), change.specification());

                if (ownerRefIdChanged != null && ownerRefIdChanged.value() != null && !ownerRefIdChanged.value().isEmpty()
                        && ownerRefTypeChanged != null && ownerRefTypeChanged.value() != null && !ownerRefTypeChanged.value().isEmpty()) {
                    // Re-hydrate property
                    this.setOwner(
                            new DomainEntity(new IdentifierStringBased(ownerRefTypeChanged.value(), ownerRefIdChanged.value())).reference());
                }

                // --- TENANT REFERENCE MUTATION ---
                org.cybnity.framework.domain.Attribute tenantRefIdChanged = EventSpecification.findSpecificationByName(Attribute.TENANT_REFERENCE_ID.name(), change.specification());
                org.cybnity.framework.domain.Attribute tenantRefTypeChanged = EventSpecification.findSpecificationByName(Attribute.TENANT_REFERENCE_IDENTIFIER_NAME.name(), change.specification());
                if (tenantRefIdChanged != null && tenantRefIdChanged.value() != null && !tenantRefIdChanged.value().isEmpty()
                        && tenantRefTypeChanged != null && tenantRefTypeChanged.value() != null && !tenantRefTypeChanged.value().isEmpty()) {
                    // Re-hydrate property
                    this.setTenant(new DomainEntity(new IdentifierStringBased(tenantRefTypeChanged.value(), tenantRefIdChanged.value())).reference());
                }
            } catch (ImmutabilityException ie) {
                throw new IllegalArgumentException(ie);
            }
        }
    }

    /**
     * Redefine the copied elements in immutable version of this account instance.
     */
    @Override
    public Serializable immutable() throws ImmutabilityException {
        Account account = new Account(parent(), new LinkedHashSet<>(this.identifiers()));
        account.occurredAt = this.occurredAt();
        account.owner = this.owner();
        account.tenant = this.tenant();
        return account;
    }

    /**
     * Implement the generation of version hash regarding this class type.
     */
    @Override
    public String versionHash() {
        return String.valueOf(serialVersionUID);
    }

    @Override
    public Identifier identified() {
        return IdentifierStringBased.build(this.identifiers());
    }

    /**
     * Get the reference of the owner of this account.
     *
     * @return A reference immutable version. Null when problem of immutable
     * reference generation.
     */
    public EntityReference owner() {
        try {
            return (EntityReference) this.owner.immutable();
        } catch (Exception e) {
            // Shall never arrive. Add implementation failure log
        }
        return null;
    }

    /**
     * Update the assigned owner.
     * Add DomainEventType.ACCOUNT_CHANGED change event into the lifecycle history.
     *
     * @param ownerRef Reference to the assigned owner. If null, the method ignore the
     *                 requested change and maintain the already existent state.
     */
    private void setOwner(EntityReference ownerRef) {
        if (ownerRef != null) {
            try {
                this.owner = ownerRef;
                // Add a change event into the history regarding modified owner
                ConcreteDomainChangeEvent changeEvt = prepareChangeEventInstance(DomainEventType.ACCOUNT_CHANGED);
                // Add owner reference changed into description of change
                Identifier id = this.owner.getEntity().identified();
                changeEvt.appendSpecification(new org.cybnity.framework.domain.Attribute(Attribute.OWNER_REFERENCE_IDENTIFIER_NAME.name(), id.name()));
                changeEvt.appendSpecification(new org.cybnity.framework.domain.Attribute(Attribute.OWNER_REFERENCE_ID.name(), id.value().toString()));
                addChangeEvent(changeEvt); // Add to changes history
            } catch (ImmutabilityException ie) {
                logger().log(Level.SEVERE, ie.getMessage(), ie);
            }
        }
    }

    /**
     * Get the reference of the tenant where this account is usable.
     *
     * @return A reference immutable version, or null when unknown.
     */
    public EntityReference tenant() {
        if (this.tenant != null) {
            try {
                return (EntityReference) this.tenant.immutable();
            } catch (Exception e) {
                // Shall never arrive. Add implementation failure log
            }
        }
        return null;
    }

    /**
     * Update the assigned tenant.
     * Add DomainEventType.ACCOUNT_CHANGED change event into the lifecycle history.
     *
     * @param tenantRef Reference to the assigned tenant. If null, the method ignore the
     *                  requested change and maintain the already existent state.
     */
    private void setTenant(EntityReference tenantRef) {
        if (tenantRef != null) {
            try {
                this.tenant = tenantRef;
                // Add a change event into the history regarding modified tenant
                ConcreteDomainChangeEvent changeEvt = prepareChangeEventInstance(DomainEventType.ACCOUNT_CHANGED);
                // Add tenant reference changed into description of change
                Identifier id = this.tenant.getEntity().identified();
                changeEvt.appendSpecification(new org.cybnity.framework.domain.Attribute(Attribute.TENANT_REFERENCE_ID.name(), id.value().toString()));
                changeEvt.appendSpecification(new org.cybnity.framework.domain.Attribute(Attribute.TENANT_REFERENCE_IDENTIFIER_NAME.name(), id.value().toString()));
                addChangeEvent(changeEvt); // Add to changes history
            } catch (ImmutabilityException ie) {
                logger().log(Level.SEVERE, ie.getMessage(), ie);
            }
        }
    }

    /**
     * Define the generation rule of identifier based on original child id name or
     * BaseConstants.IDENTIFIER_ID.name().
     */
    @Override
    protected Identifier generateIdentifierPredecessorBased(Entity predecessor, Identifier childOriginalId) throws IllegalArgumentException {
        return Predecessors.generateIdentifierPredecessorBased(predecessor, childOriginalId);
    }

    /**
     * Define the generation rule of identifier based on original child identifiers
     * name or BaseConstants.IDENTIFIER_ID.name().
     */
    @Override
    protected Identifier generateIdentifierPredecessorBased(Entity predecessor, Collection<Identifier> childOriginalIds) throws IllegalArgumentException {
        return Predecessors.generateIdentifierPredecessorBased(predecessor, childOriginalIds);
    }

    @Override
    public void handle(Command command, IContext iContext) throws IllegalArgumentException {
        throw new IllegalArgumentException("not implemented!");
    }

    @Override
    public Set<String> handledCommandTypeVersions() {
        return null;
    }

    /**
     * Get the serial version UID of this class type.
     * @return A serial version UID.
     */
    public static long serialVersionUID() {
        return serialVersionUID;
    }
}
