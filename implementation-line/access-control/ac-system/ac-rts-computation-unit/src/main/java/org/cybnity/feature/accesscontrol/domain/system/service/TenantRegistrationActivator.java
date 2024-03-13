package org.cybnity.feature.accesscontrol.domain.system.service;

import org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.TenantTransactionsRepository;
import org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.TenantsReadModelImpl;
import org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.TenantsStore;
import org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.TenantsWriteModelImpl;
import org.cybnity.accesscontrol.ciam.domain.model.TenantsWriteModel;
import org.cybnity.accesscontrol.domain.service.api.ITenantRegistrationService;
import org.cybnity.accesscontrol.domain.service.api.ciam.ITenantTransactionProjection;
import org.cybnity.accesscontrol.domain.service.api.ciam.ITenantsReadModel;
import org.cybnity.accesscontrol.domain.service.impl.TenantRegistration;
import org.cybnity.framework.IContext;
import org.cybnity.framework.application.vertx.common.service.AbstractServiceActivator;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.IDescribed;
import org.cybnity.framework.domain.model.SessionContext;
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
     * @param client                                   Optional Users Interactions Space client interactions with other domain during event processing, and/or dead letter channel notification.
     * @param context                                  Mandatory configuration context of the processing unit.
     * @param serviceName                              Optional logical name of the service to activate.
     * @param featureTenantsChangesNotificationChannel Optional channel managed by registration service for notification of Tenants changes (e.g created, removed).
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public TenantRegistrationActivator(UISAdapter client, IContext context, String serviceName, Channel featureTenantsChangesNotificationChannel) throws IllegalArgumentException {
        this.client = client;
        if (context == null) throw new IllegalArgumentException("Context parameter is required!");

        // --- Initialization of the tenant read-model and write-model reused by the registration service ---

        // Event store managing the tenant streams persistence layer
        TenantsStore tenantWriteModelPersistenceLayer = TenantsStore.instance();
        // Repository managing the tenant read-model projections
        TenantTransactionsRepository tenantsRepository = TenantTransactionsRepository.instance();

        // Prepare the processing component based on write-model and read-model persistence systems
        ITenantsReadModel tenantsReadModel = new TenantsReadModelImpl(tenantWriteModelPersistenceLayer, tenantsRepository, tenantWriteModelPersistenceLayer);
        ITenantTransactionProjection tenantsProjection = (ITenantTransactionProjection) tenantsReadModel.getProjection(ITenantTransactionProjection.class);
        TenantsWriteModel tenantsWriteModel = TenantsWriteModelImpl.instance(tenantWriteModelPersistenceLayer);

        // Define the application service (and collaboration components) able to process the event according to business/treatment rules
        processor = new TenantRegistration(new SessionContext(/* none pre-registered tenant is defined or usable by the registration service*/null), tenantsWriteModel, tenantsProjection, serviceName, featureTenantsChangesNotificationChannel, client);
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
