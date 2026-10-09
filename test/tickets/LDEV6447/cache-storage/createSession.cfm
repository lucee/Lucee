<cfscript>
	session.user_id = createUUID();
	echo( serializeJSON( {
		sessionId: session.sessionid,
		cfid: session.cfid,
		cftoken: session.cftoken
	} ) );
</cfscript>
