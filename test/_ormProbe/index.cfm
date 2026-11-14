<cfscript>
	// Building the SessionFactory is what fails when the installed hibernate extension cannot
	// initialize (LDEV-6897: Hibernate's JakartaJsonIntegration needs a JSON-B provider).
	ormGetSessionFactory();
	writeOutput( "ORM_PROBE_OK" );
</cfscript>
