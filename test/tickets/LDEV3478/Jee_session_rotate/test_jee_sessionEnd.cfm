<cfscript>
	initialSessionId = session.sessionid;
	sessionRotate();
	echo( initialSessionId );
</cfscript>