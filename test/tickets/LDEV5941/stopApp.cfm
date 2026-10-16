<cfscript>
	// Clear session from memory (not from cache storage) so the next request reloads from storage.
	try {
		cfid = session.cfid;
		factory = getPageContext().getCFMLFactory();
		scopeContext = factory.getScopeContext();
		appName = getPageContext().getApplicationContext().getName();
		scopeContext.remove( 1, appName, cfid ); // 1 = Scope.SCOPE_SESSION
		echo( "cleared" );
	}
	catch( any e ) {
		rethrow;
	}
</cfscript>
