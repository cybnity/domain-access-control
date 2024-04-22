package org.cybnity.application.accesscontrol.ui.api;

import org.cybnity.framework.domain.model.IDomainModel;

/**
 * Access Control application domain model definition.
 */
public class AccessControlDomainModel implements IDomainModel {

    @Override
    public String domainName() {
        return "ac";
    }
}
