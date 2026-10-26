<cfscript>
	initialSessionId = session.sessionid;
	if ( !structKeyExists( url, "rotateOnSessionStart" ) )
		sessionRotate();
	echo( serializeJSON( {
		initialSessionId: initialSessionId,
		sessionId: session.sessionid,
		applicationName: application.applicationName
	} ) );
</cfscript>
