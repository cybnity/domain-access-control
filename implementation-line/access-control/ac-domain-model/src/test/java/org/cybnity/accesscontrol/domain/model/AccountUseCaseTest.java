package org.cybnity.accesscontrol.domain.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.cybnity.accesscontrol.domain.model.sample.writemodel.SampleDataProvider;
import org.cybnity.accesscontrol.iam.domain.model.Account;
import org.cybnity.accesscontrol.iam.domain.model.Person;
import org.cybnity.framework.domain.IdentifierStringBased;
import org.cybnity.framework.domain.model.DomainEntityImpl;
import org.cybnity.framework.domain.model.Tenant;
import org.cybnity.framework.immutable.BaseConstants;
import org.cybnity.framework.immutable.Entity;
import org.cybnity.framework.immutable.EntityReference;
import org.cybnity.framework.immutable.HistoryState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Test of behaviors regarding the Account class.
 * 
 * @author olivier
 *
 */
public class AccountUseCaseTest {

    private Entity accountParent;
    private Account account;
    private Person owner;
    private Tenant companySubscription;

    @BeforeEach
    public void initAccountSample() throws Exception {
	accountParent = new DomainEntityImpl(
		new IdentifierStringBased(BaseConstants.IDENTIFIER_ID.name(), UUID.randomUUID().toString()));
	owner = new Person(
		new DomainEntityImpl(
			new IdentifierStringBased(BaseConstants.IDENTIFIER_ID.name(), UUID.randomUUID().toString())),
		new IdentifierStringBased(BaseConstants.IDENTIFIER_ID.name(), UUID.randomUUID().toString()));
	companySubscription = SampleDataProvider.createTenant();
	account = new Account(accountParent,
		new IdentifierStringBased(BaseConstants.IDENTIFIER_ID.name(), UUID.randomUUID().toString()),
		new EntityReference(owner.parent(), /* none relation */ null, HistoryState.COMMITTED),
		new EntityReference(companySubscription.parent(), null, HistoryState.COMMITTED));
    }

    @AfterEach
    public void cleanAccountSample() {
	accountParent = null;
	account = null;
	owner = null;
	companySubscription = null;
    }

    /**
     * Verify that any missing mandatory parameter of constructor is intercepted.
     */
    @Test
    public void givenMissingParameter_whenConstructor_thenIllegalArgumentException() {
	assertThrows(IllegalArgumentException.class, new Executable() {
	    @Override
	    public void execute() throws Throwable {
		// Check rejected instantiation about undefined accountOwnerIdentity parameter
		new Account(accountParent, /* optional parameter */ null,
			/* missing mandatory identity of account owner */ null, /* optional tenant reference */ null);
	    }
	});
    }

    /**
     * Verify that all default values are defined in instantiated account since
     * valid parameters.
     */
    @Test
    public void givenAccountValidParameters_whenConstructor_thenDefaultValue() throws Exception {
	assertNotNull(account.identified());
	assertNotNull(account.occurredAt());
	assertNotNull(account.owner());
	assertNotNull(account.tenant());
	assertNotNull(account.parent());
    }

    /**
     * Verify that creation of immutable version include all required elements.
     * 
     * @throws Exception
     */
    @Test
    public void givenInstance_whenImmutable_completedCopy() throws Exception {
	Account copy = (Account) account.immutable();
	assertNotNull(copy.parent());
	assertNotNull(copy.owner());
	assertNotNull(copy.tenant());
    }
}
