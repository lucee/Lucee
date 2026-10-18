<cfscript>
	initialSessionId = session.sessionid;
	sessionInvalidate();
	echo( serializeJSON( {
		sessionId: initialSessionId,
		onSessionEndCalls: server.LDEV4166_ended_CFML_Sessions[ initialSessionId ] ?: 0
	} ) );
</cfscript>
