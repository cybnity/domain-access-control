package org.cybnity.feature.accesscontrol.domain.system.service;

import org.cybnity.accesscontrol.ciam.domain.infrastructure.impl.TenantsRepository;
import org.cybnity.accesscontrol.domain.infrastructure.impl.IdentitiesRepository;
import org.cybnity.accesscontrol.domain.service.api.ITenantRegistrationService;
import org.cybnity.accesscontrol.domain.service.impl.TenantRegistration;
import org.cybnity.accesscontrol.iam.domain.infrastructure.impl.AccountsRepository;
import org.cybnity.framework.IContext;
import org.cybnity.framework.application.vertx.common.service.AbstractServiceActivator;
import org.cybnity.framework.domain.Command;
import org.cybnity.framework.domain.IDescribed;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.UISAdapter;

/**
 * Tenant registration service activator implementing an embedded mode of TenantRegistration component.
 */
public class TenantRegistrationActivator extends AbstractServiceActivator {

    private final UISAdapter client;

    /**
     * Configuration context of the processing unit.
     */
    private final IContext context;

    private String serviceLogicalName;

    /**
     * Default constructor.
     *
     * @param client      Optional Users Interactions Space client interactions with other domain during event processing, and/or dead letter channel notification.
     * @param context     Mandatory configuration context of the processing unit.
     * @param serviceName Optional logical name of the service to activate.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public TenantRegistrationActivator(UISAdapter client, IContext context, String serviceName) throws IllegalArgumentException {
        this.client = client;
        if (context == null) throw new IllegalArgumentException("Context parameter is required!");
        this.context = context;
        this.serviceLogicalName = serviceName;
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
            // Define the application service (and collaboration components) able to process the event according to business/treatment rules
            ITenantRegistrationService processor = new TenantRegistration(context, TenantsRepository.getInstance(), IdentitiesRepository.getInstance(), AccountsRepository.getInstance(), serviceLogicalName);

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
