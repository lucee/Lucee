<cfscript>
	settings = getApplicationSettings();
	result = {
		"hasSessionCommitInterval": structKeyExists( settings, "sessionCommitInterval" ),
		"sessionCommitInterval": structKeyExists( settings, "sessionCommitInterval" ) ? settings.sessionCommitInterval.getMillis() : 0,
		"sessionTimeoutMs": settings.sessionTimeout.getMillis()
	};

	content type="application/json";
	writeOutput( serializeJson( result ) );
</cfscript>
