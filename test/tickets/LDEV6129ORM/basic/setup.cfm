<cfscript>
	// Drop and recreate all ORM tables — ensures no stale data from previous runs
	ormReload();
</cfscript>
