<cfscript>
	function hasSessionScope( required string cfid ) {
		var sessions = getPageContext().getCFMLFactory().getScopeContext().getAllCFSessionScopes();
		return structKeyExists( sessions, application.applicationName )
			&& structKeyExists( sessions[ application.applicationName ], arguments.cfid );
	}

	session.user_id = createUUID();
	oldSessionId = session.sessionid;
	oldCfid = session.cfid;

	sessionInvalidate();

	newCfid = getPageContext().getCFID();
	result = {
		oldSessionId: oldSessionId,
		oldSessionExists: hasSessionScope( oldCfid ),
		cfidChanged: oldCfid != newCfid,
		replacementCreatedEagerly: hasSessionScope( newCfid )
	};

	result.newSessionId = session.sessionid;
	result.hasUserId = structKeyExists( session, "user_id" );
	result.sessionCreatedOnAccess = hasSessionScope( newCfid );

	echo( serializeJSON( result ) );
</cfscript>
