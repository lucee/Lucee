<cfscript>
	// Simple ORM load — just proves we can get a connection from the pool.
	// If a previous request leaked the only connection (maxTotal=1), this will throw.
	result = entityLoad( "LDEV6129Person" );
	writeOutput( "ok:#arrayLen( result )#" );
</cfscript>
