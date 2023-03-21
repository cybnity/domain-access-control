package org.cybnity.accesscontrol.domain.model.sample.writemodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.UUID;

import org.cybnity.accesscontrol.domain.model.ActivityState;
import org.cybnity.accesscontrol.domain.model.Tenant;
import org.cybnity.accesscontrol.domain.model.sample.writemodel.OrganizationDescriptor.PropertyAttributeKey;
import org.cybnity.framework.domain.IdentifierStringBased;
import org.cybnity.framework.immutable.BaseConstants;
import org.cybnity.framework.immutable.HistoryState;
import org.cybnity.framework.immutable.Identifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Unit test regarding the behavior of a Tenant class.
 * 
 * @author olivier
 *
 */
public class TenantUseCaseTest {

    private Tenant tenant;
    private String organisationName = "CYBNITY";
    private OrganizationDescriptor organization;
    private Identifier id;

    @BeforeEach
    public void initTenantSample() {
	id = new IdentifierStringBased(BaseConstants.IDENTIFIER_ID.name(), UUID.randomUUID().toString());
	// Create named tenant
	tenant = new Tenant(id);

	// Define attributes of tenant owner
	HashMap<String, Object> organisationAttr = new HashMap<String, Object>();
	organisationAttr.put(PropertyAttributeKey.Name.name(), organisationName);
	organization = new OrganizationDescriptor(tenant, organisationAttr, HistoryState.COMMITTED);
	tenant.setName(organization);
    }

    @AfterEach
    public void cleanTenantSample() {
	this.tenant = null;
	this.organization = null;
	this.id = null;
    }

    /**
     * Test that when a named tenant (original name) is upgraded regarding its
     * naming mutable property, the attribute versions history is managed by the
     * tenant.
     * 
     * @throws Exception
     */
    @Test
    public void givenDefineName_whenSetName_thenMutableVersionsHistorized() throws Exception {
	// Created tenant with a name (sample)

	// Define a new name changed regarding the tenant (e.g simulate a company brand
	// change)
	String renamedAs = "Men in Black corp";
	HashMap<String, Object> attr = new HashMap<String, Object>();
	attr.put(PropertyAttributeKey.Name.name(), renamedAs);

	OrganizationDescriptor renamed1 = new OrganizationDescriptor(tenant, attr, HistoryState.COMMITTED,
		/*
		 * prior link about old names automatically included into the versions history
		 * of this renaming
		 */ (OrganizationDescriptor) tenant.name());

	// Update the tenant with new name (that have already previous name in history
	// attached)
	tenant.setName(renamed1);
	// Verify saved name regarding the tenant
	assertEquals(renamed1, tenant.name());
	// Verify that previous name is maintained into the tenant mutable property
	// history
	assertEquals(1, tenant.name().changesHistory().size(),
		"Initial name of the tenant shall had been maintained in the versions history!");
    }

    /**
     * Verify that a created tenant include all required values after instantiation.
     */
    @Test
    public void givenDefaultTenant_whenCreate_thenCompleted() throws Exception {
	// Check valid identifier
	assertNotNull(tenant.identified());
	assertEquals(id, tenant.identified(), "Invalid sample identifier!");
	assertNotNull(tenant.occurredAt());

	// Named tenant check
	OrganizationDescriptor orgaDesc = (OrganizationDescriptor) tenant.name();
	assertNotNull(orgaDesc);
	assertEquals(organisationName, orgaDesc.getOrganizationName(),
		"Shall be defined equals as defined by the initTenantSample() call!");

	// Default status is unknown
	assertNull(tenant.status(), "Shall not be automatically defined by constructor!");
    }

    /**
     * Create a default tenant, activate (simulate its registration as active) and
     * verify that status is synchronized.
     */
    @Test
    public void givenDefaultTenant_whenActivate_thenActiveStatus() throws Exception {
	// Activate the tenant sample
	tenant.activate();
	// Check that active status if defined
	ActivityState status = tenant.status();
	assertNotNull(status, "Shall had been initialized!");
	// Check default value of the state
	assertTrue(status.isActive());
    }

    /**
     * Create a default tenant, deactivate (simulate its registration as not active)
     * and verify that status is synchronized.
     */
    @Test
    public void givenDefaultTenant_whenDeactivate_thenUnactiveStatus() throws Exception {
	// Deactivate the tenant sample
	tenant.deactivate();
	// Check that active status if defined
	ActivityState status = tenant.status();
	assertNotNull(status, "Shall had been initialized!");
	// Check default value of the state
	assertFalse(status.isActive());
    }

    /**
     * Verify that constructor supports only specific/standard identifier name and
     * reject other type (required to be equals with the used identifier name of
     * identified() ).
     */
    @Test
    public void givenInvalidIdentifierName_whenConstructor_thenIllegalArgumentException() {
	// Check that invalid identifier name is rejected
	assertThrows(IllegalArgumentException.class, new Executable() {
	    @Override
	    public void execute() throws Throwable {
		// Try instantiation based on not supported identifier name
		new Tenant(new IdentifierStringBased("other", UUID.randomUUID().toString()));
	    }
	});
	// Check null id is not supported
	assertThrows(IllegalArgumentException.class, new Executable() {
	    @Override
	    public void execute() throws Throwable {
		// Try instantiation based on none identifier
		new Tenant(null);
	    }
	});
    }
}
