<cfscript>
	// Store a CFC in session — internal changes to CFC properties are invisible to Lucee's change tracking.
	session.data = new SessionData();
	session.data.value = "committed";

	// sessionCommit writes to storage and markStored() clears the dirty flag + rebaselines the hash.
	sessionCommit();

	// Nested mutation AFTER commit — not detected by hasChanges flag, not detected by hash (CFC ref unchanged).
	session.data.value = "changedAfterCommit";

	echo( serializeJSON( { nestedValue: "committed" } ) );

	// At request end: touchAfterRequest fires, but hasChanges is false (cleared by markStored) and hash matches.
	// No redundant write. "changedAfterCommit" stays in memory only, never persisted.
</cfscript>
