package org.cybnity.keycloak.domain.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder pattern implementation class allowing to respect the build rules of Keycloak regarding a RealmRepresentation object, with decorator pattern usage added to the preparation phase of a Realm.
 *
 * @author olivier
 */
public class RealmWithDefaultExtendedResourcesBuilder extends RealmBuilder {

    public static String X_FRAME_OPTIONS = "xFrameOptions";

    String xFrameOptions;

    /**
     * Default constructor
     */
    public RealmWithDefaultExtendedResourcesBuilder() {
        super();
    }

    /**
     * Get a container of attributes representing extended configuration of realm resource.
     *
     * @return A set of attributes or null when none are defined.
     */
    public Map<String, String> browserSecurityHeaders() {
        // Build instance of current values expected as realm resource attributes
        Map<String, String> attributes = new HashMap<>();

        /**
         * "browserSecurityHeaders": {
         *     "contentSecurityPolicyReportOnly": "",
         *     "xContentTypeOptions": "nosniff",
         *     "referrerPolicy": "no-referrer",
         *     "xRobotsTag": "none",
         *     "xFrameOptions": "SAMEORIGIN",
         *     "contentSecurityPolicy": "frame-src 'self'; frame-ancestors 'self'; object-src 'none';",
         *     "strictTransportSecurity": "max-age=31536000; includeSubDomains"
         *   }
         */

        // Add general settings into realm attributes container
        if (xFrameOptions != null && !xFrameOptions.isBlank())
            attributes.put(RealmWithDefaultExtendedResourcesBuilder.X_FRAME_OPTIONS, xFrameOptions);

        if (!attributes.isEmpty()) return attributes;
        return null; // default null attributes
    }

    /**
     * Set the xFrameOptions for the realm regarding browser security headers.
     * Realm settings element.
     *
     * @param xFrameOptions Options to add in headers.
     * @return This builder.
     */
    public RealmWithDefaultExtendedResourcesBuilder xFrameOptions(String xFrameOptions) {
        if (xFrameOptions != null && !xFrameOptions.isBlank()) {
            this.xFrameOptions = xFrameOptions;
        }
        return this;
    }

    /**
     * Build and return prepared Realm configuration, including customization additional elements defining a default set of extended resources relative to the Realm.
     *
     * @return A RealmWithDefaultExtendedResources instance including customization elements.
     */
    public Realm build() {
        return new RealmWithDefaultExtendedResources(this);
    }
}
