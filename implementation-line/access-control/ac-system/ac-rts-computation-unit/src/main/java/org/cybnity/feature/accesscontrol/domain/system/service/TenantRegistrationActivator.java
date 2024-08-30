package org.cybnity.feature.accesscontrol.domain.system.service;

import org.cybnity.accesscontrol.domain.infrastructure.impl.TenantTransactionCollectionsRepository;
import org.cybnity.accesscontrol.domain.infrastructure.impl.TenantsStore;
import org.cybnity.accesscontrol.domain.infrastructure.impl.TenantsWriteModelImpl;
import org.cybnity.accesscontrol.domain.model.ITenantsWriteModel;
import org.cybnity.accesscontrol.domain.service.api.ITenantRegistrationService;
import org.cybnity.accesscontrol.domain.service.impl.TenantRegistration;
import org.cybnity.application.accesscontrol.adapter.api.admin.ISSOAdminAdapter;
import org.cybnity.application.accesscontrol.translator.ui.api.AccessControlDomainModel;
import org.cybnity.framework.IContext;
import org.cybnity.framework.UnoperationalStateException;
import org.cybnity.framework.application.vertx.common.service.AbstractServiceActivator;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.IDescribed;
import org.cybnity.framework.domain.model.SessionContext;
import org.cybnity.infastructure.technical.persistence.store.impl.redis.PersistentObjectNamingConvention;
import org.cybnity.infastructure.technical.persistence.store.impl.redis.SnapshotRepositoryRedisImpl;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.Channel;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.UISAdapter;

/**
 * Tenant registration service activator implementing an embedded mode of TenantRegistration component.
 */
public class TenantRegistrationActivator extends AbstractServiceActivator {

    private final UISAdapter client;

    /**
     * Handled facts processing component.
     */
    private final ITenantRegistrationService processor;

    /**
     * Default constructor.
     *
     * @param uisConnector                             Optional Users Interactions Space connector with other domain during event processing, and/or dead letter channel notification.
     * @param context                                  Mandatory configuration context of the processing unit.
     * @param serviceName                              Optional logical name of the service to activate.
     * @param featureTenantsChangesNotificationChannel Optional channel managed by registration service for notification of Tenants changes (e.g created, removed).
     * @param ssoConnector                             Optional connector to SSO system.
     * @throws IllegalArgumentException    When mandatory parameter is missing.
     * @throws UnoperationalStateException When impossible instantiation of the tenant snapshots repository adapter.
     */
    public TenantRegistrationActivator(UISAdapter uisConnector, IContext context, String serviceName, Channel featureTenantsChangesNotificationChannel, ISSOAdminAdapter ssoConnector) throws IllegalArgumentException, UnoperationalStateException {
        this.client = uisConnector;
        if (context == null) throw new IllegalArgumentException("Context parameter is required!");

        // --- Initialization of the tenant read-model and write-model reusable by the Tenant registration service ---
        // Event store managing the tenant streams persistence layer
        TenantsStore tenantDomainPersistenceLayer = new TenantsStore(context, new AccessControlDomainModel(), PersistentObjectNamingConvention.NamingConventionApplicability.TENANT, new SnapshotRepositoryRedisImpl(context));
        // Repository managing the tenant read-model projections
        TenantTransactionCollectionsRepository tenantReadModelProjectionsProvider = TenantTransactionCollectionsRepository.instance(context, tenantDomainPersistenceLayer);
        // Prepare the processing component based on write-model and read-model persistence systems
        ITenantsWriteModel tenantsWriteModelManager = TenantsWriteModelImpl.instance(tenantDomainPersistenceLayer);

        // Define the application service (and collaboration components) able to process the event according to business/treatment rules
        processor = new TenantRegistration(new SessionContext(/* none pre-registered tenant is defined or usable by the registration service*/null), tenantsWriteModelManager, tenantReadModelProjectionsProvider, serviceName, featureTenantsChangesNotificationChannel, uisConnector, ssoConnector);
    }

    /**
     * This implementation method is using an embedded ITenantRegistrationService to process the event when this one is supported.
     *
     * @param fact Mandatory fact to process.
     * @return True when this type of event can be handled. Else return false.
     */
    @Override
    public boolean process(IDescribed fact) {
        if (canHandle(fact)) {
            try {
                // Execute the process rules on the received event
                processor.handle((Command) fact);
            } catch (IllegalArgumentException iae) {
                moveToDeadLetterChannel(fact, iae.getMessage());
            }
            return true;
        } else {
            moveToInvalidMessageChannel(fact, "The fact type is not supported by " + this.getClass().getSimpleName());
        }
        return false;
    }

    @Override
    protected boolean canHandle(IDescribed fact) {
        return (fact != null && /* Check that only Command event type is supported by the registration processing */ Command.class.isAssignableFrom(fact.getClass()));
    }

    @Override
    protected void moveToInvalidMessageChannel(IDescribed unprocessedEvent, String cause) {
        if (unprocessedEvent != null) {
            // Temporary put into log system
            String msg = unprocessedEvent.type().value();
            if (cause != null) msg += ": " + cause;
            logger().warning(msg);
            if (client != null) {
                // TODO replace temp log by feeding of invalid message channel
            }
        }
    }

    @Override
    protected void moveToDeadLetterChannel(IDescribed unprocessedEvent, String cause) {
        if (unprocessedEvent != null) {
            // Temporary put into log system
            String msg = unprocessedEvent.type().value();
            if (cause != null) msg += ": " + cause;
            logger().warning(msg);
            if (client != null) {
                // TODO replace temp log by feeding of dead letter channel
            }
        }
    }
}
