<cfscript>
	systemoutput("hasCFSession:" & getPageContext().hasCFSession(), true);
	initialSessionId = session.sessionid;
	if ( !structKeyExists( url, "rotateOnSessionStart" ) )
		sessionRotate();
	echo( initialSessionId );
</cfscript>