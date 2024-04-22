package org.cybnity.accesscontrol.domain.service.impl;

import org.cybnity.accesscontrol.ciam.domain.model.TenantsWriteModel;
import org.cybnity.accesscontrol.domain.service.api.ApplicationServiceOutputCause;
import org.cybnity.accesscontrol.domain.service.api.ITenantRegistrationService;
import org.cybnity.accesscontrol.domain.service.api.ciam.ITenantTransactionProjection;
import org.cybnity.accesscontrol.domain.service.api.model.TenantTransaction;
import org.cybnity.application.accesscontrol.ui.api.event.AttributeName;
import org.cybnity.application.accesscontrol.ui.api.event.CommandName;
import org.cybnity.application.accesscontrol.ui.api.event.DomainEventType;
import org.cybnity.application.accesscontrol.ui.api.event.TenantRegistrationAttributeName;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.*;
import org.cybnity.framework.domain.application.ApplicationService;
import org.cybnity.framework.domain.event.DomainEventFactory;
import org.cybnity.framework.domain.event.EventSpecification;
import org.cybnity.framework.domain.model.DomainEntity;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.domain.model.TenantBuilder;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.framework.immutable.ImmutabilityException;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.Channel;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.UISAdapter;
import org.cybnity.infrastructure.technical.message_bus.adapter.impl.redis.MessageMapperFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application service implementation component relative to registration of tenant business object.
 */
public class TenantRegistration extends ApplicationService implements ITenantRegistrationService {

    /**
     * Technical logger about component activities.
     */
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Runtime context provider of service configuration.
     */
    private final ISessionContext context;

    /**
     * Logical name of this executed service.
     */
    private final String serviceName;
    private final ITenantTransactionProjection tenantsReadModel;
    private final TenantsWriteModel tenantsWriteModel;
    private final Channel tenantsChangesNotificationChannel;
    private final UISAdapter client;

    /**
     * Default constructor.
     *
     * @param context                           Mandatory execution context allowing read of settings required by this service.
     * @param tenantsStore                      Mandatory store of Tenants.
     * @param tenantsProjection                 Mandatory repository of Tenants.
     * @param serviceName                       Optional logical name of the service to activate.
     * @param tenantsChangesNotificationChannel Optional output channel to feed about changed tenants (e.g created, removed, changed).
     * @param client                            Optional client to Users Interactions Space.
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     */
    public TenantRegistration(ISessionContext context, TenantsWriteModel tenantsStore, ITenantTransactionProjection tenantsProjection, String serviceName, Channel tenantsChangesNotificationChannel, UISAdapter client) throws IllegalArgumentException {
        super();
        if (tenantsStore == null) throw new IllegalArgumentException("tenantsStore parameter is required!");
        this.tenantsWriteModel = tenantsStore;
        if (tenantsProjection == null) throw new IllegalArgumentException("tenantsProjection parameter is required!");
        this.tenantsReadModel = tenantsProjection;
        if (context == null) throw new IllegalArgumentException("Context parameter is required!");
        this.context = context;
        this.serviceName = serviceName;
        this.client = client;
        this.tenantsChangesNotificationChannel = tenantsChangesNotificationChannel;
    }

    @Override
    public void handle(Command command) throws IllegalArgumentException {
        try {
            if (command != null) {
                // Check that command is a request of organization registration
                if (CommandName.REGISTER_TENANT.name().equals(command.type().value())) {
                    // --- INPUT VALIDATION ---
                    // Read and check the organization name to register
                    Attribute tenantNamingAtt = EventSpecification.findSpecificationByName(TenantRegistrationAttributeName.TENANT_NAMING.name(), command.specification());
                    if (tenantNamingAtt == null)
                        throw new IllegalArgumentException("Organization naming attribute shall be defined!");
                    String tenantName = tenantNamingAtt.value();
                    if (tenantName == null || tenantName.isEmpty())
                        throw new IllegalArgumentException("Organization naming attribute value shall be defined!");
                    DomainEvent commandResponse;

                    // --- PROCESSING RULES ---
                    // Search if existing tenant already registered in domain
                    TenantTransaction existingOrganizatonTenant = tenantsReadModel.findByLabel(tenantName, /* search tenant in any in operational status avoiding duplicated tenants with same name */ null, this.context);

                    if (existingOrganizatonTenant != null) {
                        // Existing registered tenant is identified and known by access control domain
                        // RULE : de-duplication rule about existing Tenant that is already in operational activity
                        if (existingOrganizatonTenant.activityStatus != null && existingOrganizatonTenant.activityStatus) {
                            // CASE: tenant (e.g platform tenant with same name and already in an operational activity status not re-assignable) creation is not authorized AND REJECTION SHALL BE NOTIFIED
                            commandResponse = prepareCommonResponseEvent(DomainEventType.TENANT_REGISTRATION_REJECTED, command, existingOrganizatonTenant.label, existingOrganizatonTenant.activityStatus, existingOrganizatonTenant.identifiedBy);
                            // Set precision about cause of rejection
                            commandResponse.appendSpecification(new Attribute(org.cybnity.framework.domain.event.AttributeName.OUTPUT_CAUSE_TYPE.name(), ApplicationServiceOutputCause.EXISTING_TENANT_ALREADY_ASSIGNED.name()));
                        } else {
                            // CASE: tenant (e.g platform tenant with same name and that is not in an operational activity status, and could potentially be re-assignable) return as known AND ELIGIBLE TO RE-ASSIGNMENT
                            // Prepare and return new tenant actioned event
                            commandResponse = prepareCommonResponseEvent(DomainEventType.TENANT_REGISTERED, command, existingOrganizatonTenant.label, existingOrganizatonTenant.activityStatus, existingOrganizatonTenant.identifiedBy);
                        }
                    } else {
                        // None existing tenant with same organization name
                        // CASE: create a new Tenant
                        // Read optional definition of tenant activity state
                        Attribute isActiveTenantAtt = EventSpecification.findSpecificationByName(AttributeName.ACTIVITY_STATE.name(), command.specification());
                        Boolean activeTenantToCreate = (isActiveTenantAtt != null) ? Boolean.valueOf(isActiveTenantAtt.value()) : /* Default not active tenant */ Boolean.FALSE;
                        // Create the new tenant item
                        commandResponse = addTenant(tenantName, tenantNamingAtt, command, activeTenantToCreate);
                    }

                    if (commandResponse != null) {
                        // Notify response to command sender
                        if (this.tenantsChangesNotificationChannel != null && this.client != null) {
                            // Notify the general output channel regarding new actioned tenant
                            try {
                                this.client.publish(commandResponse, tenantsChangesNotificationChannel, new MessageMapperFactory().getMapper(IDescribed.class, String.class));
                            } catch (Exception e) {
                                logger.log(Level.SEVERE, "Impossible notification of organization tenant registration result!", e);
                            }
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Invalid type of command which is not managed by " + this.getClass().getSimpleName() + "!");
                }
            } else {
                throw new IllegalArgumentException("Command parameter is required!");
            }
        } catch (ImmutabilityException ime) {
            // Impossible execution caused by a several code problem
            logger.log(Level.SEVERE, "Impossible handle(Command command) method response!", ime);
        } catch (UnoperationalStateException e) {
            // Problem during the persistence system usage
            logger.log(Level.SEVERE, "Impossible handle(Command command) for cause of persistence system in non operational state!", e);
        }
    }

    /**
     * Add a new tenant domain object into the aggregates store.
     *
     * @param tenantLabel    Mandatory name of the tenant to create into store.
     * @param tenantNaming   Optional attribute regarding the requested tenant name to register when defined (e.g received by service).
     * @param originEvent    Mandatory origin event handled by the service and which can be referenced as predecessor.
     * @param isActiveTenant Optional activity status of tenant to create.
     * @return Actioned organization event including tenant description (e.g potential additional configuration and/or technical information).
     * @throws IllegalArgumentException    When mandatory parameter is not defined.
     * @throws ImmutabilityException       When usage of immutable version of content have a problem avoiding its usage.
     * @throws UnoperationalStateException When problem of persistence system usage.
     */
    private DomainEvent addTenant(String tenantLabel, Attribute tenantNaming, Command originEvent, Boolean isActiveTenant) throws IllegalArgumentException, ImmutabilityException, UnoperationalStateException {
        if (tenantLabel == null || tenantLabel.isEmpty())
            throw new IllegalArgumentException("tenantLabel parameter is required and shall not be empty!");
        if (originEvent == null) throw new IllegalArgumentException("originEvent parameter is required!");

        // TODO implementation of verification that equals real name is existing and accessible/usable from UIAM server (e.g Keycloak connector)

        // RealmRepresentation organizationRealm = buildRealmRepresentation(configurationSettings)

        // verify existing realm registered (and access control settings recorded as usable via UIAM connector adapter configuration)
        // RealmResource existingRes = realm(String realmName)
        // existingRes equals organization named

        // --- EXECUTE THE WRITE MODEL CHANGE ---
        // Create new Tenant fact
        TenantBuilder builder = new TenantBuilder(tenantLabel, /* Predecessor event of new tenant creation */ originEvent.getIdentifiedBy(), isActiveTenant);
        builder.buildInstance();
        Tenant tenant = builder.getResult();

        // --- EXECUTE THE WRITE MODEL CHANGE ---
        // Append new tenant into write model stream
        this.tenantsWriteModel.add(tenant);// Read-model is automatically notified

        // Prepare and return new tenant actioned event
        return prepareCommonResponseEvent(DomainEventType.TENANT_REGISTERED, originEvent, tenant.label().getLabel(), tenant.status().isActive(), tenant.identified().value().toString());
    }

    /**
     * Prepare and build a type of event including all standard attributes promise by this registration service as output event.
     *
     * @param eventTypeToPrepare   Mandatory type of event to build.
     * @param originEvent          Mandatory origin event handled by the service and which can be referenced as predecessor.
     * @param tenantLabel          Optional attribute regarding the requested organization name to register when defined (e.g received by service).
     * @param tenantActivityStatus Optional activity status of tenant.
     * @param tenantIdentifier     Mandatory identifier of tenant.
     * @return A prepared event including specifications.
     * @throws ImmutabilityException    When usage of immutable version of content have a problem avoiding its usage.
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     */
    private DomainEvent prepareCommonResponseEvent(DomainEventType eventTypeToPrepare, Command originEvent, String tenantLabel, Boolean tenantActivityStatus, String tenantIdentifier) throws ImmutabilityException, IllegalArgumentException {
        if (eventTypeToPrepare == null) throw new IllegalArgumentException("eventTypeToPrepare parameter is required!");
        if (originEvent == null) throw new IllegalArgumentException("originEvent parameter is required!");
        if (tenantIdentifier == null || tenantIdentifier.isEmpty())
            throw new IllegalArgumentException("Tenant identifier parameter is required!");
        // Prepare event's unique identifier referencing origin command event
        LinkedHashSet<Identifier> evtIDBasedOn = new LinkedHashSet<>();
        evtIDBasedOn.add(IdentifierStringBased.generate(null));// technical technical uid
        // Add origin command identifier as contributor to the event identification value
        Identifier originCmdId = originEvent.identified();
        if (originCmdId != null)
            evtIDBasedOn.add(originCmdId);
        DomainEntity eventUID = new DomainEntity(evtIDBasedOn);

        // Prepare event specifications
        Collection<Attribute> definition = new ArrayList<>();
        // Set existing origin event's correlation id
        Attribute originCorrelationId = originEvent.correlationId();
        if (originCorrelationId != null && originCorrelationId.value() != null) {
            Attribute correlationIdAtt = new Attribute(Command.CORRELATION_ID, originCorrelationId.value());
            EventSpecification.appendSpecification(correlationIdAtt, definition);
        }
        // Add origin organization name requested for registration
        if (tenantLabel != null && !tenantLabel.isEmpty()) {
            definition.add(new Attribute(TenantRegistrationAttributeName.TENANT_NAMING.name(), tenantLabel));
        }

        // Set the logical name of this pipeline which is sender of the event
        if (this.serviceName != null && !serviceName.isEmpty())
            definition.add(new Attribute(org.cybnity.framework.domain.event.AttributeName.SERVICE_NAME.name(), serviceName));

        // Set activity status
        if (tenantActivityStatus != null) {
            // Set precision about the existing tenant description synthesis (tenant's identifier, and status)
            definition.add(new Attribute(AttributeName.ACTIVITY_STATE.name(), tenantActivityStatus.toString()));
        }

        // Set tenant uid
        definition.add(new Attribute(AttributeName.TENANT_ID.name(), tenantIdentifier));

        // Build instance
        return DomainEventFactory.create(eventTypeToPrepare.name(), eventUID, definition, originEvent.reference(), /* none changedModelElementRef */ null);
    }

}
