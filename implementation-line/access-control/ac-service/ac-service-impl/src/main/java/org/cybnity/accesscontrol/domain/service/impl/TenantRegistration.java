package org.cybnity.accesscontrol.domain.service.impl;

import org.cybnity.accesscontrol.ciam.domain.model.TenantsReadModel;
import org.cybnity.accesscontrol.domain.service.api.ApplicationServiceOutputCause;
import org.cybnity.accesscontrol.domain.service.api.ITenantRegistrationService;
import org.cybnity.accesscontrol.iam.domain.model.AccountsReadModel;
import org.cybnity.accesscontrol.iam.domain.model.IdentitiesReadModel;
import org.cybnity.application.accesscontrol.ui.api.event.AttributeName;
import org.cybnity.application.accesscontrol.ui.api.event.CommandName;
import org.cybnity.application.accesscontrol.ui.api.event.DomainEventType;
import org.cybnity.application.accesscontrol.ui.api.event.TenantRegistrationAttributeName;
import org.cybnity.framework.IContext;
import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.DomainEvent;
import org.cybnity.framework.domain.IdentifierStringBased;
import org.cybnity.framework.domain.application.ApplicationService;
import org.cybnity.framework.domain.event.DomainEventFactory;
import org.cybnity.framework.domain.event.EventSpecification;
import org.cybnity.framework.domain.model.DomainEntity;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.Identifier;
import org.cybnity.framework.immutable.ImmutabilityException;

import java.io.Serializable;
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
    private final IContext context;
    /**
     * Logical name of this executed service.
     */
    private String serviceName;
    private final TenantsReadModel tenantsRepository;
    private final IdentitiesReadModel identitiesRepository;
    private final AccountsReadModel accountsRepository;

    /**
     * Default constructor.
     *
     * @param context              Mandatory execution context allowing read of settings required by this service.
     * @param tenantsRepository    Mandatory repository of Tenants.
     * @param identitiesRepository Optional repository of Identities.
     * @param accountsRepository   Mandatory repository of Accounts.
     * @param serviceName          Optional logical name of the service to activate.
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     */
    public TenantRegistration(IContext context, TenantsReadModel tenantsRepository, IdentitiesReadModel identitiesRepository, AccountsReadModel accountsRepository, String serviceName) throws IllegalArgumentException {
        super();
        if (tenantsRepository == null) throw new IllegalArgumentException("tenantsRepository parameter is required!");
        this.tenantsRepository = tenantsRepository;
        if (accountsRepository == null) throw new IllegalArgumentException("accountsRepository parameter is required!");
        this.accountsRepository = accountsRepository;
        if (context == null) throw new IllegalArgumentException("Context parameter is require!");
        this.context = context;
        this.identitiesRepository = identitiesRepository;
        this.serviceName = serviceName;
    }

    @Override
    public void handle(Command command) throws IllegalArgumentException {
        try {
            if (command != null) {
                // Check that command is a request of organization registration
                if (CommandName.REGISTER_ORGANIZATION.name().equals(command.type().value())) {
                    // --- INPUT VALIDATION ---
                    // Read and check the organization name to register
                    Attribute organizationNamingAtt = EventSpecification.findSpecificationByName(TenantRegistrationAttributeName.ORGANIZATION_NAMING.name(), command.specification());
                    if (organizationNamingAtt == null)
                        throw new IllegalArgumentException("Organization naming attribute shall be defined!");
                    String organizationNaming = organizationNamingAtt.value();
                    if (organizationNaming == null || organizationNaming.isEmpty())
                        throw new IllegalArgumentException("Organization naming attribute value shall be defined!");

                    // --- PROCESSING RULES ---
                    // Search if existing tenant already registered in domain
                    Tenant existingOrganizatonTenant = tenantsRepository.findByName(organizationNaming);
                    if (existingOrganizatonTenant != null) {
                        // Existing registered tenant is identified and known by access control domain

                        // RULE : de-duplication rule about Tenant
                        // CASE: tenant (e.g social entity with same name) creation is not authorized AND REJECTION SHALL BE NOTIFIED
                        // Build DomainEventType.ORGANIZATION_REGISTRATION_REJECTED event
                        DomainEvent rejectionEvent = prepareCommonResponseEvent(DomainEventType.ORGANIZATION_REGISTRATION_REJECTED, command, organizationNamingAtt, existingOrganizatonTenant);
                        // Set precision about cause of rejection
                        rejectionEvent.appendSpecification(new Attribute(org.cybnity.framework.domain.event.AttributeName.OUTPUT_CAUSE_TYPE.name(), ApplicationServiceOutputCause.EXISTING_TENANT_ALREADY_ASSIGNED.name()));

                        // TODO Publish event to output channel
                        //uisClient.publish(requestEvent, domainIOGateway, new MessageMapperFactory().getMapper(IDescribed.class, String.class));
                    } else {
                        // None existing tenant with same organization name
                        // CASE: create a new Tenant initializing into the identities repository based on the organization name
                        DomainEvent actionedOrganizationTenantEvent = addTenant(organizationNaming, organizationNamingAtt, command);

                        // TODO Publish event to output channel
                    }
                } else {
                    throw new IllegalArgumentException("Invalid type of command which is not managed by " + this.getClass().getSimpleName());
                }
            } else {
                throw new IllegalArgumentException("Command parameter is required!");
            }
        } catch (ImmutabilityException ime) {
            // Impossible execution caused by a several code problem
            logger.log(Level.SEVERE, "Impossible handle(Command command) method response!!", ime);
        }
    }

    /**
     * Add a new tenant domain object into the repository of identities server.
     * This method use delegation connector to the Identity server (e.g Keycloak system) delegated as UIAM including tenants repository.
     *
     * @param tenantLabel        Mandatory name of the tenant to create into repositories.
     * @param organizationNaming Optional attribute regarding the requested organization name to register when defined (e.g received by service).
     * @param originEvent        Mandatory origin event handled by the service and which can be referenced as predecessor.
     * @return Actioned organization event including tenant description (e.g potential additional configuration and/or technical information).
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     * @throws ImmutabilityException    When usage of immutable version of content have a problem avoiding its usage.
     */
    private DomainEvent addTenant(String tenantLabel, Attribute organizationNaming, Command originEvent) throws IllegalArgumentException, ImmutabilityException {
        if (tenantLabel == null || tenantLabel.isEmpty())
            throw new IllegalArgumentException("tenantLabel parameter is required and shall not be empty!");

        // TODO implementation of verification that equals real name is existing and accessible/usable from UIAM server (e.g Keycloak connector)

        // RealmRepresentation organizationRealm = buildRealmRepresentation(configurationSettings)

        // verify existing realm registered (and access control settings recorded as usable via UIAM connector adapter configuration)
        // RealmResource existingRes = realm(String realmName)
        // existingRes equals organization named

        // TODO implementation of the write model change

        // create new Tenant(found organization description) into domain repository
        Tenant created = null;
        // Prepare and return new organization actioned event
        DomainEvent organizationActioned = prepareCommonResponseEvent(DomainEventType.ORGANIZATION_REGISTERED, originEvent, organizationNaming, created);

        return null;
    }

    /**
     * Prepare and build a type of event including all standard attributes promise by this registration service as output event.
     *
     * @param eventTypeToPrepare    Mandatory type of event to build.
     * @param originEvent           Mandatory origin event handled by the service and which can be referenced as predecessor.
     * @param organizationNamingAtt Optional attribute regarding the requested organization name to register when defined (e.g received by service).
     * @param tenant                Optional tenant.
     * @return A prepared event including specifications.
     * @throws ImmutabilityException    When usage of immutable version of content have a problem avoiding its usage.
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     */
    private DomainEvent prepareCommonResponseEvent(DomainEventType eventTypeToPrepare, Command originEvent, Attribute organizationNamingAtt, Tenant tenant) throws ImmutabilityException, IllegalArgumentException {
        if (eventTypeToPrepare == null) throw new IllegalArgumentException("eventTypeToPrepare parameter is required!");
        if (originEvent == null) throw new IllegalArgumentException("originEvent parameter is required!");

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
        if (organizationNamingAtt != null)
            EventSpecification.appendSpecification(organizationNamingAtt, definition);
        // Set the logical name of this pipeline which is sender of the event
        if (this.serviceName != null && !serviceName.isEmpty())
            definition.add(new Attribute(org.cybnity.framework.domain.event.AttributeName.SERVICE_NAME.name(), serviceName));

        if (tenant != null) {
            // Set precision about the existing tenant description synthesis (tenant's identifier, and status)
            Boolean status = tenant.status().isActive();
            definition.add(new Attribute(AttributeName.ACTIVITY_STATE.name(), status.toString()));
            Serializable tenantId = tenant.identified().value();
            definition.add(new Attribute(AttributeName.TENANT_ID.name(), tenantId.toString()));
        }

        // Build instance
        return DomainEventFactory.create(eventTypeToPrepare.name(), eventUID, definition, originEvent.reference(), /* none changedModelElementRef */ null);
    }

}
