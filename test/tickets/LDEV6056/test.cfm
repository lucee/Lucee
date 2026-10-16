<cfscript>
	// The bug: closure in parent calls overridden method which uses super
	service = new preside.InterceptorService();
	results = service.registerInterceptors();
	echo( results[ 1 ] );
</cfscript>
