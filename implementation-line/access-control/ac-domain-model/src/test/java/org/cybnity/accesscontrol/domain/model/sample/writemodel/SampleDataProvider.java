package org.cybnity.accesscontrol.domain.model.sample.writemodel;

import java.util.HashMap;
import java.util.UUID;

import org.cybnity.accesscontrol.domain.model.sample.writemodel.OrganizationProperty.PropertyAttributeKey;
import org.cybnity.framework.domain.IdentifierStringBased;
import org.cybnity.framework.domain.model.DomainEntityImpl;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.BaseConstants;
import org.cybnity.framework.immutable.HistoryState;
import org.cybnity.framework.immutable.Identifier;

public class SampleDataProvider {

    /**
     * Get a sample of tenant.
     * 
     * @return A sample instance.
     */
    public static Tenant createTenant() throws Exception {
	Identifier id = new IdentifierStringBased(BaseConstants.IDENTIFIER_ID.name(), UUID.randomUUID().toString());
	// Create named tenant
	Tenant tenant = new Tenant(new DomainEntityImpl(id),
		/*
		 * Simulate auto-assigned parent identifier without extension of the child id
		 * generation based on identifiers and minimum quantity of lenght
		 */ null, /* Simulate unknown original activity state */ null);

	// Define attributes of tenant owner
	HashMap<String, Object> organisationAttr = new HashMap<String, Object>();
	organisationAttr.put(PropertyAttributeKey.Name.name(), "CYBNITY France");
	OrganizationProperty organization = new OrganizationProperty(tenant.parent(), organisationAttr,
		HistoryState.COMMITTED);
	tenant.setOrganization(organization);
	return tenant;
    }
}
