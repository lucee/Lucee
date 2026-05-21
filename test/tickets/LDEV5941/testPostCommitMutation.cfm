<cfscript>
	// Store a CFC in session, commit, then mutate a nested property.
	// LDEV-5930 makes ComponentImpl.hashCode content-aware, so the hash check at request end
	// catches the post-commit mutation even though the CFC reference is unchanged — the redundant
	// write fires and "changedAfterCommit" IS persisted. The check template asserts that.
	session.data = new SessionData();
	session.data.value = "committed";

	sessionCommit();

	session.data.value = "changedAfterCommit";

	echo( serializeJSON( { nestedValue: session.data.value } ) );
</cfscript>
