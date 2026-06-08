<cfscript>
	// Reloaded from storage by the prior stopApp.cfm. Expect "committed" (from sessionCommit), not "changedAfterCommit".
	echo( serializeJSON( { nestedValue: session.data.value } ) );
</cfscript>
