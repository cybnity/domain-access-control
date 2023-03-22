package org.cybnity.accesscontrol.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.cybnity.accesscontrol.domain.model.ActivityState.PropertyAttributeKey;
import org.cybnity.framework.domain.IdentifierStringBased;
import org.cybnity.framework.immutable.BaseConstants;
import org.cybnity.framework.immutable.EntityReference;
import org.cybnity.framework.immutable.HistoryState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Behaviors unit test regarding the ActivityState class.
 * 
 * @author olivier
 *
 */
public class ActivityStateUseCaseTest {

    private EntityReference propertyOwner;

    @BeforeEach
    public void initOwner() throws Exception {
	// Create tenant
	Tenant owner = new Tenant(
		new IdentifierStringBased(BaseConstants.IDENTIFIER_ID.name(), UUID.randomUUID().toString()),
		Boolean.TRUE);
	// Deactivate by default
	owner.deactivate();
	propertyOwner = owner.reference();
    }

    @AfterEach
    public void cleanOwner() {
	this.propertyOwner = null;
    }

    /**
     * Test that valid parameter are treated by constructor and created instance is
     * including default values.
     * 
     * @throws Exception
     */
    @Test
    public void givenStateParameters_whenCreate_thenValidInstantiation() throws Exception {
	// Create an active state owner by a tenant
	ActivityState state = new ActivityState(propertyOwner, Boolean.TRUE);
	// Check that active value is saved
	assertTrue(state.isActive(), "Invalid instantiated value!");
	assertNotNull(state.occurredAt(), "Shall have been created at!");
	// Check that state is assigned to the good owner
	assertEquals(propertyOwner.getEntity(), state.owner(), "Invalid owner assigned to the state!");

	// Check that immutable version is readable and equals
	ActivityState clone = (ActivityState) state.immutable();
	assertEquals(state, clone, "Should be the equals without nanosecond consideration!");
    }

    /**
     * Test refused instantiation of activity state by constructors when missing
     * mandatory parameter(s).
     * 
     * @throws Exception
     */
    @Test
    public void givenMissingParameter_whenCreateState_thenIllegalArgumentException() throws Exception {
	// Check that instantiation of state without defined owner is refused
	assertThrows(IllegalArgumentException.class, new Executable() {

	    @Override
	    public void execute() throws Throwable {
		new ActivityState(null, Boolean.TRUE);
	    }
	}, "Missing owner parameter shall be refused!");

	// Check that refuse instantiation about unknown default active/unactive state
	// value
	assertThrows(IllegalArgumentException.class, new Executable() {
	    @Override
	    public void execute() throws Throwable {
		new ActivityState(propertyOwner, null);
	    }
	}, "Missing state value parameter shall be refused!");
    }

    /**
     * Test the valid versions history management of the property.
     * 
     * @throws Exception
     */
    @Test
    public void givenActivityStateVersion_whenUpdateVersion_thenVersionsHitorized() throws Exception {
	// Create a state without history
	ActivityState v1 = new ActivityState(propertyOwner, Boolean.TRUE);
	// Check empty history and current valid status
	assertTrue(v1.isActive());
	assertTrue(v1.changesHistory().isEmpty());

	// Create another version with changed of status (simulate a temporary unactive
	// status of a subject) from constructor that attach directly prior version
	ActivityState v2 = new ActivityState(propertyOwner.getEntity(),
		ActivityState.buildPropertyValue(PropertyAttributeKey.StateValue, Boolean.FALSE),
		HistoryState.CANCELLED, v1);
	assertFalse(v2.isActive());
	// Check that history of previous versions is not lost and was updated into the
	// V2
	assertEquals(1, v2.changesHistory().size(), "Only v1 previous version shall exist in history!");
	// Check that v2 state was updated as cancelled version
	assertTrue(HistoryState.CANCELLED == v2.historyStatus(),
		"enhanced V2 instance shall had been automatically setted during the enhancement executed method!");

	// Create another version with again changed state
	ActivityState v3 = new ActivityState(propertyOwner.getEntity(),
		ActivityState.buildPropertyValue(PropertyAttributeKey.StateValue, Boolean.FALSE), HistoryState.MERGED,
		v2);
	// Enhance history about all old versions chain
	v2.enhanceHistoryOf(v3, null /* without change of new version state */);
	assertTrue(HistoryState.MERGED == v3.historyStatus(), "State shall had not been modified during enhancement!");

	// Check that all previous versions are saved into its history attribute
	assertEquals(2, v3.changesHistory().size(), "Invalid lost history versions!");
	// Verify if previous version were saved without modification
	assertTrue(v3.changesHistory().contains(v1));// Based on equals evaluation
	assertTrue(v3.changesHistory().contains(v2));// Based on equals evaluation
	assertFalse(v3.changesHistory().contains(v3),
		"Current version shall not be recorded as duplicata into its history!");

	// Create another version with only prior last version (without all old versions
	// chain)
	ActivityState v4 = new ActivityState(propertyOwner.getEntity(),
		ActivityState.buildPropertyValue(PropertyAttributeKey.StateValue, Boolean.FALSE),
		HistoryState.COMMITTED, v3);
	// Check that only one previous version is saved as prioi into its history
	// attribute
	assertEquals(1, v4.changesHistory().size(), "Invalid historized prior quantity!");
    }
}
