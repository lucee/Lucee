<cfscript>
	param name="url.log";
	param name="url.token";
	try {
		throw( message="ldev6598 #url.token#" );
	}
	catch ( e ) {
		// the datadog layout needs an exception
		cflog( text="ldev6598 #url.token#", log=url.log, type="information", exception=e );
	}
	echo( getTimeZone().getID() );
</cfscript>
