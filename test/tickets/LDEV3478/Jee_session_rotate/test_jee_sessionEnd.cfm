<cfscript>
	initialSessionId = session.sessionid;
	if ( !structKeyExists( url, "rotateOnSessionStart" ) )
		sessionRotate();
	echo( initialSessionId );
</cfscript>