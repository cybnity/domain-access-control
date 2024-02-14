package org.cybnity.accesscontrol.domain.service.impl;

import org.cybnity.accesscontrol.ciam.domain.model.TenantsReadModel;
import org.cybnity.accesscontrol.domain.service.ITenantRegistrationService;
import org.cybnity.accesscontrol.domain.service.TenantRegistrationServiceConfigurationVariable;
import org.cybnity.accesscontrol.iam.domain.model.AccountsReadModel;
import org.cybnity.accesscontrol.iam.domain.model.IdentitiesReadModel;
import org.cybnity.accesscontrol.iam.domain.model.MailAddress;
import org.cybnity.application.accesscontrol.ui.api.event.AttributeName;
import org.cybnity.application.accesscontrol.ui.api.event.CommandName;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.domain.Attribute;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.application.ApplicationService;
import org.cybnity.framework.domain.event.EventSpecification;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.ImmutabilityException;

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
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     */
    public TenantRegistration(IContext context, TenantsReadModel tenantsRepository, IdentitiesReadModel identitiesRepository, AccountsReadModel accountsRepository) throws IllegalArgumentException {
        super();
        if (tenantsRepository == null) throw new IllegalArgumentException("tenantsRepository parameter is required!");
        this.tenantsRepository = tenantsRepository;
        if (accountsRepository == null) throw new IllegalArgumentException("accountsRepository parameter is required!");
        this.accountsRepository = accountsRepository;
        if (context == null) throw new IllegalArgumentException("Context parameter is require!");
        this.context = context;
        this.identitiesRepository = identitiesRepository;
    }

    @Override
    public void handle(Command command) throws IllegalArgumentException {
        try {
            if (command != null) {
                // Check that command is a request of organization registration
                if (CommandName.REGISTER_ORGANIZATION.name().equals(command.type().value())) {
                    // --- INPUT VALIDATION ---
                    // Read and check the organization name to register
                    Attribute organizationNamingAtt = EventSpecification.findSpecificationByName(AttributeName.OrganizationNaming.name(), command.specification());
                    if (organizationNamingAtt == null)
                        throw new IllegalArgumentException("Organization naming attribute shall be defined!");
                    String organizationNaming = organizationNamingAtt.value();
                    if (organizationNaming == null || organizationNaming.isEmpty())
                        throw new IllegalArgumentException("Organization naming attribute value shall be defined!");

                    // --- PROCESSING RULES ---
                    // Search if existing tenant already registered including existing a minimum one active user account
                    Tenant existingOrganizatonTenant = tenantsRepository.findTenant(organizationNaming, /* including existing activated accounts */ true);
                    if (existingOrganizatonTenant != null) {
                        // Existing registered tenant is identified with already assigned and used by a minimum one activated user account

                        // RULE : de-duplication rule about Tenant
                        if (isAuthorizedTenantReassignment(existingOrganizatonTenant)) {
                            // Confirmed re-assigning tenant to organization

                            // Read how many user accounts are already active on the found Tenant
                            // with verified email address (as usable and owned by account owners)
                            Integer accountsQty = accountsRepository.accountsCount(existingOrganizatonTenant.identified(), MailAddress.Status.VERIFIED);
                            if (accountsQty != null) {
                                // Return Tenant found including verified accounts quantity
                            }
                        } else {
                            // Re-assignment of the found tenant is not authorized
                            // return OrganizationRegistrationRejected event
                        }
                    } else {
                        // None existing tenant with same organization name

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
        }
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
