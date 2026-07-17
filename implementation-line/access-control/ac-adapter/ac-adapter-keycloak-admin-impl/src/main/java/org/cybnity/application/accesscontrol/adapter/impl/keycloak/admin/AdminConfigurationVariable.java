package org.cybnity.application.accesscontrol.adapter.impl.keycloak.admin;

import org.cybnity.framework.IReadableConfiguration;
import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

/**
 * Enumeration defining a set of variables regarding an accessible facade of Keycloak server.
 * <p>
 * The configuration of each value regarding each environment variable enum, is
 * managed into the Helm values.yaml file regarding the executable system which
 * need to declare the environment variables as available for usage via this set
 * of enum.
 *
 * @author olivier
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_8370_CM6")
public enum AdminConfigurationVariable implements IReadableConfiguration {

    /**
     * Full URL of the Keycloak URL (e.g; http://dev-deploy.cybnity.tech:8081)
     * Path value shall include communication protocol, dns or ip address of server, and port.
     */
    KEYCLOAK_SERVER_URL("KEYCLOAK_SERVER_URL"),

    /**
     * Administration account username allowing authentication to the default Keycloak realm.
     */
    REALM_MASTER_USERNAME("REALM_MASTER_USERNAME"),

    /**
     * Administration account password allowing authentication to the default Keycloak realm.
     */
    REALM_MASTER_PASSWORD("REALM_MASTER_PASSWORD"),

    /**
     * Keycloak clientId (e.g; admin-cli) required for connection to Keycloak Admin API.
     */
    REALM_MASTER_CLIENTID("REALM_MASTER_CLIENTID"),

    /**
     * Grant type (e.g; password) defined for master realm usage clientId according to the authentication flow defined into Keycloak server.
     */
    REALM_MASTER_GRANT_TYPE("REALM_MASTER_GRANT_TYPE"),

    /**
     * Name of Keycloak default master realm (e.g; defined by default into the Keycloak server configuration for server administration) allowing administration of extended realms.
     */
    REALM_MASTER_NAME("REALM_MASTER_NAME"),

    /**
     * Security defense headers parameters regarding a realm.
     * See <a href="https://datatracker.ietf.org/doc/html/rfc7034#section-2.2.1">X-Frame-Options </a> for more details
     * See <a href="https://wjw465150.gitbooks.io/keycloak-documentation/content/server_admin/topics/threat/clickjacking.html">mitigation of Clickjacking</a> for help
     */
    REALM_DEFAULT_SECURITY_HEADER_XFRAME_OPTIONS("REALM_DEFAULT_SECURITY_HEADER_XFRAME_OPTIONS"),

    /**
     * Frontend page url regarding a new realm.
     */
    REALM_DEFAULT_FRONTEND_URL("REALM_DEFAULT_FRONTEND_URL"),

    /**
     * See <a href="https://www.w3.org/TR/CSP/#directive-frame-src">frame-src</a> to restrict the URLS which may be loaded into nested browsing contexts.
     * See <a href="https://www.w3.org/TR/CSP/#directive-frame-ancestors">frame-ancestors</a> to define the URLs which can embed the resource using frame of iframe.
     * See <a href="https://www.w3.org/TR/CSP/#directive-object-src">object-src</a> to restrict URLS from which plugin context may be loaded.
     */
    REALM_DEFAULT_SECURITY_HEADER_CONTENT_SECURITY_POLICY("REALM_DEFAULT_SECURITY_HEADER_CONTENT_SECURITY_POLICY")
    ;

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
    AdminConfigurationVariable(String aName) throws IllegalArgumentException {
        if (aName == null || "".equalsIgnoreCase(aName))
            throw new IllegalArgumentException("The name of this variable shall be defined!");
        this.name = aName;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
