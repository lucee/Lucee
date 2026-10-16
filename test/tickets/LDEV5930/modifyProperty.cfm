<cfscript>
	// Modify component property via accessor - this does NOT directly modify session keys
	session.user.setUsername( url.username ?: "modified_user" );
	session.user.setRole( url.role ?: "admin" );

	result = {
		sessionId: session.sessionid,
		username: session.user.getUsername(),
		role: session.user.getRole()
	};
	echo( serializeJSON( result ) );
</cfscript>
