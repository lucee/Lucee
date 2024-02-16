<cfscript>
	initialSessionId = session.sessionid;
	sessionInvalidate();
	echo( initialSessionId );
</cfscript>