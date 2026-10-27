<cfscript>
	echo( serializeJSON( {
		sessionId: session.sessionid,
		hasUserId: structKeyExists( session, "user_id" )
	} ) );
</cfscript>
