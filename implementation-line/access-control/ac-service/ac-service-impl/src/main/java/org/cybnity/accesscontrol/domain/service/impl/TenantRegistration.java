package org.cybnity.accesscontrol.domain.service.impl;

import org.cybnity.accesscontrol.ciam.domain.model.TenantsReadModel;
import org.cybnity.accesscontrol.domain.service.api.ApplicationServiceOutputCause;
import org.cybnity.accesscontrol.domain.service.api.ITenantRegistrationService;
import org.cybnity.accesscontrol.domain.service.api.TenantRegistrationServiceConfigurationVariable;
import org.cybnity.accesscontrol.iam.domain.model.AccountsReadModel;
import org.cybnity.accesscontrol.iam.domain.model.IdentitiesReadModel;
import org.cybnity.accesscontrol.iam.domain.model.MailAddress;
import org.cybnity.application.accesscontrol.ui.api.event.AttributeName;
import org.cybnity.application.accesscontrol.ui.api.event.CommandName;
import org.cybnity.application.accesscontrol.ui.api.event.DomainEventType;
import org.cybnity.application.accesscontrol.ui.api.event.TenantRegistrationAttributeName;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;
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
                    // Search if existing tenant already registered including existing a minimum one active user account
                    Tenant existingOrganizatonTenant = identitiesRepository.findTenant(organizationNaming, /* including existing activated accounts */ true);
                    if (existingOrganizatonTenant != null) {
                        // Existing registered tenant is identified with already assigned and used by a minimum one activated user account

                        // RULE : de-duplication rule about Tenant
                        if (isAuthorizedTenantReassignment(existingOrganizatonTenant)) {
                            // CASE: Confirm re-assignable tenant to organization
                            // Build event regarding existing tenant able to be re-assigned
                            DomainEvent organizationActioned = prepareCommonResponseEvent(DomainEventType.ORGANIZATION_REASSIGNMENT_ELIGIBLE, command, organizationNamingAtt);

                            // TODO Publish event to output channel

                        } else {
                            // CASE: Re-assignment of the found tenant is not authorized AND REJECTION SHALL BE NOTIFIED
                            // Build DomainEventType.ORGANIZATION_REGISTRATION_REJECTED event
                            DomainEvent rejectionEvent = prepareCommonResponseEvent(DomainEventType.ORGANIZATION_REGISTRATION_REJECTED, command, organizationNamingAtt);
                            // Set precision about cause of rejection
                            rejectionEvent.appendSpecification(new Attribute(AttributeName.OUTPUT_CAUSE_TYPE.name(), ApplicationServiceOutputCause.EXISTING_TENANT_ALREADY_ASSIGNED.name()));

                            // Read how many user accounts are already active on the found Tenant
                            // with verified email address (as usable and owned by account owners)
                            Integer accountsQty = accountsRepository.accountsCount(existingOrganizatonTenant.identified(), MailAddress.Status.VERIFIED);
                            if (accountsQty != null) {
                                // Set precision about found verified accounts quantity
                                rejectionEvent.appendSpecification(new Attribute(TenantRegistrationAttributeName.ACTIVE_ACCOUNTS_COUNT.name(), accountsQty.toString()));
                            }

                            // TODO Publish event to output channel
                            //uisClient.publish(requestEvent, domainIOGateway, new MessageMapperFactory().getMapper(IDescribed.class, String.class));
                        }
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
        } catch (UnoperationalStateException use) {
            // Impossible execution caused by a several code problem
            logger.log(Level.SEVERE, "Impossible handle(Command command) method execution!", use);
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

        // TODO implementation of the write model change and registration ot Realm into the identity server with collect of realm's setting allowing connector dynamic configuration

        // RealmRepresentation organizationRealm = buildRealmRepresentation(configurationSettings)

        // create(organizationRealm) in identity repository

        // get success realm registration confirmation (and access control settings recorded)
        // RealmResource existingRes = realm(String realmName)
        // existingRes equals organization named

        // create new Tenant(found organization description) into domain repository

        // Prepare and return new organization actioned event
        DomainEvent organizationActioned = prepareCommonResponseEvent(DomainEventType.ORGANIZATION_REGISTERED, originEvent, organizationNaming);

        return null;
    }

    /**
     * Prepare and build a type of event including all standard attributes promise by this registration service as output event.
     *
     * @param eventTypeToPrepare    Mandatory type of event to build.
     * @param originEvent           Mandatory origin event handled by the service and which can be referenced as predecessor.
     * @param organizationNamingAtt Optional attribute regarding the requested organization name to register when defined (e.g received by service).
     * @return A prepared event including specifications.
     * @throws ImmutabilityException    When usage of immutable version of content have a problem avoiding its usage.
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     */
    private DomainEvent prepareCommonResponseEvent(DomainEventType eventTypeToPrepare, Command originEvent, Attribute organizationNamingAtt) throws ImmutabilityException, IllegalArgumentException {
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
            definition.add(new Attribute(AttributeName.SERVICE_NAME.name(), serviceName));

        // Build instance
        return DomainEventFactory.create(eventTypeToPrepare.name(), eventUID, definition, originEvent.reference(), /* none changedModelElementRef */ null);
    }

    /**
     * Check if an existing tenant can be reassigned to new owner according to existing verified and active user accounts, and/or re-assignment configuration settings.
     * It is a de-duplication rule realization about tenant which could have been registered during a previous attempt of user account creation, but which have never been finalized with success.
     *
     * @param tenant Mandatory tenant to evaluate for re-assignment eligibility.
     * @return True when the tenant can be re-assigned to new owner.
     * @throws IllegalArgumentException    When mandatory parameter is missing.
     * @throws UnoperationalStateException Can be also thrown in case of impossible read of tenant status (e.g immutability exception). Can be thrown in case of missing service configuration environment variable read from context.
     */
    private boolean isAuthorizedTenantReassignment(Tenant tenant) throws IllegalArgumentException, UnoperationalStateException {
        if (tenant == null) throw new IllegalArgumentException("Tenant parameter is required!");
        boolean authorizedReassignment = true;
        try {
            if (tenant.status().isActive()) {
                // Check the re-assignment feature setting
                Object authorizedReassignmentConfig = context.get(TenantRegistrationServiceConfigurationVariable.TENANT_REGISTRATION_AUTHORIZED_REASSIGNMENT.getName());
                if (authorizedReassignmentConfig != null && Boolean.class.isAssignableFrom(authorizedReassignmentConfig.getClass())) {
                    // Potential unauthorized re-assignment according to the capability defined configuration
                    authorizedReassignment = (Boolean) authorizedReassignmentConfig;
                } else {
                    // Unknown configuration problem generating untrusted service runtime
                    // So stop execution
                    throw new UnoperationalStateException("Missing service configuration variable (" + TenantRegistrationServiceConfigurationVariable.TENANT_REGISTRATION_AUTHORIZED_REASSIGNMENT.getName() + ")!");
                }
            }
        } catch (ImmutabilityException ie) {
            logger.log(Level.SEVERE, "isAuthorizedTenantReassignment() exception relative to tenant status impossible read!", ie);
            throw new UnoperationalStateException(ie);
        }
        return authorizedReassignment;
    }
}
