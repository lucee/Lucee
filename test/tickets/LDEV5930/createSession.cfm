<cfscript>
	// First request: session starts, onSessionStart stores component
	result = {
		sessionId: session.sessionid,
		cfid: session.cfid,
		cftoken: session.cftoken,
		username: session.user.getUsername(),
		role: session.user.getRole()
	};
	echo( serializeJSON( result ) );
</cfscript>
