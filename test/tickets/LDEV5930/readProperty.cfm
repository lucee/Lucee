<cfscript>
	// Read component property - with sessionCluster=true this reloads from DB
	result = {
		sessionId: session.sessionid,
		username: session.user.getUsername(),
		role: session.user.getRole()
	};
	echo( serializeJSON( result ) );
</cfscript>
