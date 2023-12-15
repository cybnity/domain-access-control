package org.cybnity.application.accesscontrol.domain.system.gateway.service;

import org.cybnity.application.accesscontrol.ui.api.event.CommandName;
import org.cybnity.framework.Context;
import org.cybnity.framework.IContext;
import org.cybnity.framework.domain.*;
import org.cybnity.framework.domain.application.ConfigurationViolation;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Processor ensuring a local treatment relative to an event.
 * For example, can be responsible to perform a UI capability in the current thread of the component requesting the execution.
 */
public class LocalProcessingUnitExecutor implements ProcessingUnitDelegation {

    /**
     * Technical logging
     */
    private final Logger logger;

    /**
     * Referential of classes that are able to process some specific types of events in embedded
     * mode and that can be dynamically loaded from the runtime context (e.g based on this application configuration).
     */
    private static Map<String, String> capabilityProviderClass;

    public LocalProcessingUnitExecutor() {
        super();
        // Init the logger
        logger = Logger.getLogger(this.getClass().getName());
        initLocalProcessingUnits();
    }

    /**
     * Initialize the list of class names that are defined as executable in embedded mode
     * according to a configuration file, and aligned with their feature library dependency
     * attached into the packaged project.
     */
    private void initLocalProcessingUnits() {
        // TODO fichier de resource (bundle propertie) + dependences maven des jars de features concernés
        // commencer avec une configuration supportant CommandName.REGISTER_ORGANIZATION.name() et une UI capability traitant ce type d'event

        // ajouter un check de sécurité au moment du contrôle d'état opérationnel de la gateway pour détecter tout problème de config entre la resource de config et l'existance réelle du lib en classpath
        // ajouter ce check également en TU pour le développeur avant packaging de livraison

        // Read the configuration file
        // Identify each event type names supported for local processing
        // Identify each class which is responsible to process the event type

        // Save local capability provider definition for dynamic activation by the process(...) method
        capabilityProviderClass = new HashMap<>();
    }

    /**
     * Get the list of event types that are defined as to be treated according to this delegate.
     *
     * @return A collection of event type names, or empty list.
     */
    public static Collection<String> supportedEventNames() {
        // Read all the fact event type names of the referential
        Collection<String> managed = new ArrayList<>(capabilityProviderClass.keySet());
        return Collections.unmodifiableCollection(managed);
    }

    @Override
    public void process(IDescribed factEvent) throws IllegalArgumentException {
        if (factEvent == null) throw new IllegalArgumentException("factEvent parameter is required!");
        try {
            Attribute factType = factEvent.type();
            if (factType != null) {
                String factTypeName = factType.value();
                if (!factTypeName.isEmpty()) {
                    String fullyQualifiedClassName = getFullyQualifiedClassName(factTypeName);
                    if (fullyQualifiedClassName != null && !fullyQualifiedClassName.isEmpty()) {
                        // Resolve capability component dynamically from classpath
                        Class<?> embeddedClass = new DependencyClassLoader().getProcessingUnitClass(fullyQualifiedClassName);
                        // Create the processor instance
                        IService computationUnit = (IService) embeddedClass.getDeclaredConstructor(new Class[]{}).newInstance();
                        // Execute the command treatment via the delegated component
                        computationUnit.handle(factEvent, new Context());
                    } else {
                        // None local computation unit class is defined in configuration as eligible for support of event type processing
                        logger.log(Level.WARNING, ConfigurationViolation.MISSING_DEPENDENCY_ARTIFACT.name() + " : none event type processor class is defined in configuration to support the processing of event type (" + factTypeName + ")!");
                    }
                } else {
                    // Unknown type name can't be supported for processing unit identification
                    logger.log(Level.SEVERE, ConformityViolation.UNIDENTIFIED_EVENT_TYPE.name() + " : impossible identification of processing unit able to support undefined event type name!");
                }
            }
        } catch (ClassNotFoundException cnfe) {
            // Local computation unit class is not found from current application classpath
            logger.log(Level.SEVERE, ConfigurationViolation.MISSING_DEPENDENCY_ARTIFACT.name() + " : " + cnfe.getMessage());
        } catch (Exception e) {
            // Multiple possible exception during attempt of delegate class instantiation
            // - Unavailable default constructor defined by the delegate class
            // - Impossible access to the delegate's constructor for cause of security exception
            // - Instantiation exception during constructor execution
            // - ClassCastException of the delegate instance which is not implementing ICommandHandler
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * Find the class name of capability provider supporting the treatment responsibility of an event type.
     *
     * @param factTypeName Mandatory event type name declared as supported by a computation unit available in classpath.
     * @return A class name of computation unit. Or null when none found from capability providers referential.
     */
    private static String getFullyQualifiedClassName(String factTypeName) {
        String fullyQualifiedClassName = null;
        // Identify the class name of component which is able to process the event type
        // Search in features library (is embedded according to the classpath current configuration based on the packaged project) which UI capability shall be executed
        for (Map.Entry<String, String> providerConfig : capabilityProviderClass.entrySet()) {
            // Identify the event type declared as supported by a computation component
            if (providerConfig.getKey().equals(factTypeName)) {
                // Identify the declared computation component class name supporting the event type
                fullyQualifiedClassName = providerConfig.getValue();
            }
        }
        return fullyQualifiedClassName;
    }
}
