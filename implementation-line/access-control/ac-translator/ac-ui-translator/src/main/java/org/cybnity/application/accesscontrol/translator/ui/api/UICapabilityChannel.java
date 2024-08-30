package org.cybnity.application.accesscontrol.translator.ui.api;


import org.cybnity.infrastructure.technical.message_bus.adapter.api.ICapabilityChannel;
import org.cybnity.infrastructure.technical.message_bus.adapter.api.NamingConventions;

/**
 * Channel regarding a Capabilities Domain exposed as an entry point via the
 * User Interactions Space.
 * Each channel name shall be a action capability (e.g registration, deletion).
 */
public enum UICapabilityChannel implements ICapabilityChannel {

    /**
     * Boundary regarding the capabilities of Access Control domain IO entrypoint.
     */
    access_control_in(new AccessControlDomainModel().domainName() + NamingConventions.STREAM_NAME_SEPARATOR + "in"),

    /**
     * Boundary regarding the feature (input channel) managing a tenant registration.
     */
    access_control_tenant_registration(new AccessControlDomainModel().domainName() + NamingConventions.STREAM_NAME_SEPARATOR + "tenant_registration"),

    /**
     * Boundary regarding the changes performed on Tenant aggregates.
     * Pub/Sub channel allowing listening by multiple consumers that are interesting by tenants evolution (e.g created, modified, removed).
     */
    access_control_tenants_changes(new AccessControlDomainModel().domainName() + NamingConventions.CHANNEL_NAME_SEPARATOR + "tenants_changes"),
    /**
     * Boundary regarding the feature managing the processing unit presence announces (e.g input channel for any PU declaration as eligible delegate responsible for treatment of specific event types).
     * Pub/Sub channel allowing listening by multiple consumers.
     */
    access_control_pu_presence_announcing(new AccessControlDomainModel().domainName() + NamingConventions.CHANNEL_NAME_SEPARATOR + "pu_presence_announcing"),

    /**
     * Boundary regarding the promotion of the dynamic recipients list changes that are managed by a domain IO Gateway about registered routing paths.
     * Pub/Sub channel allowing listening by multiple consumers.
     */
    access_control_io_gateway_dynamic_routing_plan_evolution(new AccessControlDomainModel().domainName() + NamingConventions.CHANNEL_NAME_SEPARATOR + "io_gateway_routing_plan_evolution");

    private final String shortName;

    UICapabilityChannel(String acronym) {
        this.shortName = acronym;
    }

    /**
     * Get the short label (e.g acronym) regarding this channel.
     *
     * @return An acronym or null.
     */
    @Override
    public String shortName() {
        return this.shortName;
    }
}
