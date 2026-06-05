package org.cybnity.keycloak.domain.model;

import org.cybnity.framework.support.annotation.Requirement;
import org.cybnity.framework.support.annotation.RequirementCategory;

import java.util.Collection;

/**
 * Represents a realm resource.
 *
 * @author olivier
 *
 */
@Requirement(reqType = RequirementCategory.Security, reqId = "REQ_SEC_COMMON_IA_2")
public class Realm {

    /**
     * Default constructor.
     *
     * @param name Mandatory label.
     * @throws IllegalArgumentException When mandatory parameter is missing.
     */
    public Realm(String name)
            throws IllegalArgumentException {
        if (name == null)
            throw new IllegalArgumentException("The name parameter is required!");

    }
}
