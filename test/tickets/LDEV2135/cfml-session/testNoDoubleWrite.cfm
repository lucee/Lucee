<cfscript>
	// Store a CFC in session - internal changes to CFC properties are invisible to Lucee's change tracking
	session.data = new SessionData();
	session.data.value = "committed";

	// Commit the session - this writes to storage and calls setClean()
	sessionCommit();

	// Sleep to allow Redis NearCache async write to complete and clear the entry from the deque
	// Without this, the NearCache holds an object reference that gets mutated below
	sleep( 100 );

	// Now change the CFC property AFTER commit
	// This should NOT trigger hasChanges because changes to CFC internals aren't detected
	session.data.value = "changedAfterCommit";

	// Return what we set (the local session still has the changed value)
	echo( serializeJSON( { nestedValue: "committed" } ) );

	// End of request: touchAfterRequest() will be called, but hasChanges should be false
	// so no write should happen, and "changedAfterCommit" should be lost
</cfscript>
