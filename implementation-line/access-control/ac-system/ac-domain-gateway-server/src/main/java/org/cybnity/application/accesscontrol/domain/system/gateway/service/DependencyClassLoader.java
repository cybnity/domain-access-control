package org.cybnity.application.accesscontrol.domain.system.gateway.service;

/**
 * Custom class loader allowing to dynamically load feature classes (e.g UI capability) from jar in dependency of this project.
 */
public class DependencyClassLoader extends ClassLoader {

    public DependencyClassLoader() {
        super();
    }

    /**
     * Find class from application dependencies, resolve it and return its prototype ready for instantiation.
     *
     * @param fullyQualifiedClassName Mandatory class name to load.
     * @return A class ready for instantiation.
     * @throws ClassNotFoundException When class name not found in current system classpath.
     */
    public Class<?> getProcessingUnitClass(String fullyQualifiedClassName) throws ClassNotFoundException {
        return loadClass(fullyQualifiedClassName, /* True for resolving. False when only existing check shall be executed */ true);
    }
}
