package org.cybnity.accesscontrol.domain.service.api;

import org.cybnity.framework.domain.application.IApplicationService;

/**
 * Application service layer responsible for Tenant registration rules and conditions checks which handle the request of new Tenant creation intent, which apply the creation condition according to existing and conformity criteria, which synchronize the eventual mapping with the other domains (e.g sub-domain relative to identity management) and which manage the request of change on the Tenant aggregate (Write Model) via dedicated change request command.
 */
public interface ITenantRegistrationService extends IApplicationService {

}
