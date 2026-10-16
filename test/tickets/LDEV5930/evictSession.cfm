<cfscript>
	// Simulate session eviction from memory by removing it from the in-memory context map
	// This forces the next request to reload from DB storage
	pc = getPageContext();
	factory = pc.getCFMLFactory();
	scopeContext = factory.getScopeContext();

	// Get the session contexts map and remove our session
	appName = application.applicationName;
	cfid = session.cfid;

	// Access cfSessionContexts via reflection - it's a private field
	field = scopeContext.getClass().getDeclaredField( "cfSessionContexts" );
	field.setAccessible( true );
	contexts = field.get( scopeContext );

	appSessions = contexts[ appName ];
	if ( !isNull( appSessions ) ) {
		appSessions.remove( cfid );
		systemOutput( "LDEV-5930: evicted session #cfid# from memory for app #appName#", true );
	}

	result = {
		evicted: true,
		cfid: cfid,
		appName: appName
	};
	echo( serializeJSON( result ) );
</cfscript>
