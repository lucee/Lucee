<cfscript>
	// Check what value was persisted to storage
	// If setClean() works correctly, this should be "committed" (from sessionCommit)
	// NOT "changedAfterCommit" (which was set after sessionCommit but before end of request)
	echo( serializeJSON( { nestedValue: session.data.value } ) );
</cfscript>
