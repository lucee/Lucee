<cfscript>
	initialSessionId = session.sessionid;
	sessionInvalidate();
	echo( serializeJSON( {
		sessionId: initialSessionId,
		onSessionEndCalls: server.LDEV4166_ended_JEE_Sessions[ initialSessionId ] ?: 0
	} ) );
</cfscript>
