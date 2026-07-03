package org.cybnity.accesscontrol;

import org.cybnity.framework.IContext;

/**
 * Composition strategy class defining the perimeter of configuration elements supporting a component behavior (e.g; domain object build service).
 * This is a declaration of common interface for any algorithmic that is really implemented by the strategy concrete classes.
 * This strategy pattern realization is focused on the configuration type usable to create concrete configuration data (e.g; configuration elements required commonly for any Realm creation).
 */
public abstract class ConfigurationStrategy {

    /**
     * Prepare a type of configuration object including default and commons settings.
     *
     * @param ctx  Mandatory context eventually including elements required during the object preparation runtime.
     * @param args Optional configuration elements and or logical contents that can be used during the preparation process.
     * @return The expected type of object instance including common and default configuration elements.
     * @throws IllegalArgumentException When mandatory parameter is missing or is invalid.
     */
    public abstract Object prepare(IContext ctx, Object... args) throws IllegalArgumentException;
}
