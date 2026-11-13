<cfscript>
	initialSessionId = session.sessionid;
	sessionInvalidate();
	echo( serializeJSON( {
		"sessionId": initialSessionId,
		"applicationName": application.applicationName,
		"ended": server.LDEV3320_ended[ initialSessionId ] ?: {}
	} ) );
</cfscript>
