package org.cybnity.accesscontrol.iam.domain.infrastructure.impl;

import org.cybnity.framework.IReadableConfiguration;

/**
 * Enumeration defining a set of variables regarding a WriteModel of the IAM domain object types (e.g Tenant).
 * <p>
 * The configuration of each value regarding each environment variable enum, is
 * managed into the Helm values.yaml file regarding the executable system which
 * need to declare the environment variables as available for usage via this set
 * of enum.
 *
 * @author olivier
 */
public enum IAMWriteModelConfigurationVariable implements IReadableConfiguration {
    /**
     * Default duration in seconds relative to each snapshot resource of IAM perimeter that are saved into a snapshot repository.
     */
    IAM_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS("IAM_WRITEMODEL_SNAPSHOT_ITEM_DEFAULT_EXPIRATION_DURATION_IN_SECONDS");

    /**
     * Name of this environment variable currently hosted by the system environment.
     */
    private final String name;

    /**
     * Default constructor of a configuration variable that is readable from the
     * system environment variables set.
     *
     * @param aName Mandatory name of the environment variable that is readable from
     *              the current system environment (e.g defined by the runtime
     *              container or operating system).
     * @throws IllegalArgumentException When mandatory parameter is not defined.
     */
    private IAMWriteModelConfigurationVariable(String aName) throws IllegalArgumentException {
        if (aName == null || "".equalsIgnoreCase(aName))
            throw new IllegalArgumentException("The name of this variable shall be defined!");
        this.name = aName;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
