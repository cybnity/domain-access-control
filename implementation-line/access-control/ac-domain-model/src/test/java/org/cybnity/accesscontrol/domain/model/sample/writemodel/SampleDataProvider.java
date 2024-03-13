package org.cybnity.accesscontrol.domain.model.sample.writemodel;

import org.cybnity.framework.domain.IdentifierStringBased;
import org.cybnity.framework.domain.model.DomainEntity;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.domain.model.TenantDescriptor;
import org.cybnity.framework.immutable.BaseConstants;
import org.cybnity.framework.immutable.HistoryState;
import org.cybnity.framework.immutable.Identifier;

import java.util.HashMap;
import java.util.UUID;

public class SampleDataProvider {

    /**
     * Get a sample of tenant.
     *
     * @return A sample instance.
     */
    public static Tenant createTenant() throws Exception {
        Identifier id = new IdentifierStringBased(BaseConstants.IDENTIFIER_ID.name(), UUID.randomUUID().toString());
        // Create named tenant
        Tenant tenant = new Tenant(new DomainEntity(id),
                /*
                 * Simulate auto-assigned parent identifier without extension of the child id
                 * generation based on identifiers and minimum quantity of length
                 */ null, /* Simulate unknown original activity state */ null);

        // Define attributes of tenant owner
        HashMap<String, Object> organisationAttr = new HashMap<String, Object>();
        organisationAttr.put(TenantDescriptor.PropertyAttributeKey.LABEL.name(), "CYBNITY France");

        TenantDescriptor descriptor = new TenantDescriptor(tenant.parent(), organisationAttr,
                HistoryState.COMMITTED);
        tenant.setLabel(descriptor);
        return tenant;
    }
}
