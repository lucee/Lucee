<cfscript>
	// Clear session from memory (not from Redis storage) to force reload on next request
	// This is needed for testing because _InternalRequest shares the same JVM memory
	try {
		// Capture cfid BEFORE accessing scopeContext to avoid race conditions
		cfid = session.cfid;
		factory = getPageContext().getCFMLFactory();
		scopeContext = factory.getScopeContext();
		appName = getPageContext().getApplicationContext().getName();
		systemOutput( "stopApp: removing session #cfid# for app #appName#", true );
		scopeContext.remove( 1, appName, cfid ); // 1 = Scope.SCOPE_SESSION
		systemOutput( "stopApp: removed successfully", true );
		echo( "cleared" );
	}
	catch( any e ) {
		systemOutput( "stopApp error: #e.message#", true );
		rethrow;
	}
</cfscript>
