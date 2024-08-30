package org.cybnity.accesscontrol.domain.service.impl;

import org.cybnity.accesscontrol.domain.infrastructure.impl.TenantTransactionCollectionsRepository;
import org.cybnity.accesscontrol.domain.model.ITenantsWriteModel;
import org.cybnity.accesscontrol.domain.service.api.ApplicationServiceOutputCause;
import org.cybnity.accesscontrol.domain.service.api.ITenantRegistrationService;
import org.cybnity.accesscontrol.domain.service.api.event.ACApplicationQueryName;
import org.cybnity.accesscontrol.domain.service.api.model.TenantDataView;
import org.cybnity.accesscontrol.domain.service.api.model.TenantTransactionsCollection;
import org.cybnity.application.accesscontrol.adapter.api.admin.ISSOAdminAdapter;
import org.cybnity.application.accesscontrol.translator.ui.api.event.DomainEventType;
import org.cybnity.application.accesscontrol.ui.api.event.AttributeName;
import org.cybnity.application.accesscontrol.ui.api.event.CommandName;
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

import java.util.*;
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
    private final TenantTransactionCollectionsRepository tenantsReadModel;
    private final ITenantsWriteModel tenantsWriteModel;
    private final Channel tenantsChangesNotificationChannel;

    /**
     * Connector to collaboration space.
     */
    private final UISAdapter uisClient;

    /**
     * Connector to UAM subdomain (e.g usable for realm management aligned with managed Tenants)
     */
    private final ISSOAdminAdapter ssoClient;

    /**
     * Default constructor.
     *
     * @param context                           Mandatory execution context allowing read of settings required by this service.
     * @param tenantsStore                      Mandatory store of Tenants.
     * @param tenantsProjection                 Mandatory repository of Tenants.
     * @param serviceName                       Optional logical name of the service to activate.
     * @param tenantsChangesNotificationChannel Optional output channel to feed about changed tenants (e.g created, removed, changed).
     * @param uisClient                         Optional connector to Users Interactions Space.
     * @param ssoClient                         Optional connector to Single-Sign On service.
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     */
    public TenantRegistration(ISessionContext context, ITenantsWriteModel tenantsStore, TenantTransactionCollectionsRepository tenantsProjection, String serviceName, Channel tenantsChangesNotificationChannel, UISAdapter uisClient, ISSOAdminAdapter ssoClient) throws IllegalArgumentException {
        super();
        if (tenantsStore == null) throw new IllegalArgumentException("tenantsStore parameter is required!");
        this.tenantsWriteModel = tenantsStore;
        if (tenantsProjection == null) throw new IllegalArgumentException("tenantsProjection parameter is required!");
        this.tenantsReadModel = tenantsProjection;
        if (context == null) throw new IllegalArgumentException("Context parameter is required!");
        this.context = context;
        this.serviceName = serviceName;
        this.uisClient = uisClient;
        this.tenantsChangesNotificationChannel = tenantsChangesNotificationChannel;
        this.ssoClient = ssoClient;
    }

    @Override
    public void handle(Command command) throws IllegalArgumentException {
        try {
            if (command != null) {
                // TODO coding of USe case about notified realm creation that have been created by UIAM administrator over Keycloak IHM

                // Check that command is a request of organization registration
                if (CommandName.REGISTER_TENANT.name().equals(command.type().value())) {
                    // USE CASE: EXPLICIT REGISTRATION OF NEW TENANT BY FINAL USER

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
                    Map<String, String> queryParameters = new HashMap<>();
                    // Explicit query name to perform with filtering criteria definition
                    queryParameters.put(Command.TYPE, ACApplicationQueryName.TENANT_VIEW_FIND_BY_LABEL.name());
                    queryParameters.put(TenantDataView.PropertyAttributeKey.LABEL.name(), tenantName); // Search vertex (data-view) node with equals name
                    queryParameters.put(TenantDataView.PropertyAttributeKey.DATAVIEW_TYPE.name(), TenantDataView.class.getSimpleName()); // type of vertex (node type in graph model)
                    // Search tenant in any in operational status avoiding duplicated tenants with same name
                    List<TenantTransactionsCollection> tenantsCollection = tenantsReadModel.queryWhere(queryParameters, this.context);
                    TenantDataView existingOrganizatonTenant = null;
                    if (tenantsCollection != null) {
                        if (tenantsCollection.size() == 1) {
                            // Only one valid tenant data view versions history have been found in the repository
                            List<TenantDataView> existingTenantVersions = tenantsCollection.get(0).versions();
                            // Get the last known tenant data view version
                            existingOrganizatonTenant = existingTenantVersions.get(existingTenantVersions.size() - 1);
                        } else {
                            // Potential problem of consistency if several tenant data-view are existing in the repository
                            logger.log(Level.SEVERE, "Multiple tenant data view with label equals to '" + tenantName + "' are existing in the " + TenantTransactionCollectionsRepository.class.getSimpleName() + " graph model, but only unique shall be maintained up-to-date and queryable per unique node label!");
                        }
                    }
                    if (existingOrganizatonTenant != null) {
                        // Existing registered tenant is identified and known by access control domain
                        // RULE : de-duplication rule about existing Tenant that is already in operational activity
                        String statusLabel = existingOrganizatonTenant.valueOfProperty(TenantDataView.PropertyAttributeKey.ACTIVITY_STATUS);
                        Boolean activityStatus = (statusLabel != null && !statusLabel.isEmpty()) ? Boolean.valueOf(statusLabel) : null;
                        String tenantCurrentLabel = existingOrganizatonTenant.valueOfProperty(TenantDataView.PropertyAttributeKey.LABEL);
                        String tenantUUID = existingOrganizatonTenant.valueOfProperty(TenantDataView.PropertyAttributeKey.IDENTIFIED_BY);
                        if (activityStatus != null && activityStatus) {
                            // CASE: tenant (e.g platform tenant with same name and already in an operational activity status not re-assignable) creation is not authorized AND REJECTION SHALL BE NOTIFIED
                            commandResponse = prepareCommonResponseEvent(DomainEventType.TENANT_REGISTRATION_REJECTED, command, tenantCurrentLabel, activityStatus, tenantUUID);
                            // Set precision about cause of rejection
                            commandResponse.appendSpecification(new Attribute(org.cybnity.framework.domain.event.AttributeName.OUTPUT_CAUSE_TYPE.name(), ApplicationServiceOutputCause.EXISTING_TENANT_ALREADY_ASSIGNED.name()));
                        } else {
                            // CASE: tenant (e.g platform tenant with same name and that is not in an operational activity status, and could potentially be re-assignable) return as known AND ELIGIBLE TO RE-ASSIGNMENT
                            // Prepare and return new tenant actioned event
                            commandResponse = prepareCommonResponseEvent(DomainEventType.TENANT_REGISTERED, command, tenantCurrentLabel, activityStatus, tenantUUID);
                        }
                        // --- SECURITY CONTROL: SSO ACCESSIBILITY FOR REALM When realm is existing ---
                        // & GIVEN EXISTING REALM:
                        // - WHEN: check that dedicated CYBNITY systems access client is configured (e.g Keycloak connector settings) and accessible from AC read-model (e.g read of tenant equals named and existing client configuration data-view)
                        // - THEN: if not accessible configuration, start only resolution process (don't wait resolution end) allowing to manage the UIAM client configuration refresh (make SSO accessible regarding the existing tenant)
                        // TODO impl in async/parallel process without waiting its ends
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
                        if (this.tenantsChangesNotificationChannel != null && this.uisClient != null) {
                            // Notify the general output channel regarding new actioned tenant
                            try {
                                this.uisClient.publish(commandResponse, tenantsChangesNotificationChannel, new MessageMapperFactory().getMapper(IDescribed.class, String.class));
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
        } catch (UnoperationalStateException ue) {
            // Problem during the persistence system usage
            logger.log(Level.SEVERE, "Impossible handle(Command command) for cause of persistence system in non operational state!", ue);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Impossible handle(Command command) for cause of technical failure in non operational state!", e);
        }
    }

    /**
     * Add a new tenant domain object into the golden source responsible on UIAM (e.g Keycloak), manage the preparation of dedicated UIAM connector settings (making them accessible to other CYBNITY service for SSO capability support), and create new tenant into aggregates store with automatic refresh of the Access Control domain's read-model repository.
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

        // GIVEN POTENTIAL NEW REALM TO CREATE FOR SAME TENANT NAME:
        // - Search existing realm registered (and access control settings recorded as usable via UIAM connector adapter configuration)
        // RealmResource existingRes = realm(String realmName) // existingRes equals organization named
// TODO impl from ssoClient


        // --- NEW REALM REGISTRATION ---
        // GIVEN NOT EXISTING REALM:
        // - WHEN: create new realm into the UIAM system (e.g over Keycloak API; connector settings for dedicated Tenant scope; default values initialized regarding authentication and SSO features) which can be mapped with a new Tenant to register
        // RealmRepresentation organizationRealm = buildRealmRepresentation(configurationSettings)
        // TODO impl
        // - & WHEN: new realm created and ready for usage (e.g accounts creation) as SSO context
        // - THEN: create a newt Tenant fact (mapped to realm specification) including the connection settings
        TenantBuilder builder = new TenantBuilder(tenantLabel, /* Predecessor event of new tenant creation */ originEvent.getIdentifiedBy(), isActiveTenant);
        builder.buildInstance();
        Tenant tenant = builder.getResult();
        // - & THEN: update the AC write-model change with automatic update of AC read-model (e.g creation event automatically notified by the store to the read-model projections repository)
        // Append new tenant into write model stream (store as rehydratable aggregate data)
        this.tenantsWriteModel.add(tenant);// With notification for auto-refresh of read-model repository

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
