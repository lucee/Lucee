<cfscript>
	function hasSessionScope( required string cfid ) {
		var sessions = getPageContext().getCFMLFactory().getScopeContext().getAllCFSessionScopes();
		return structKeyExists( sessions, application.applicationName )
			&& structKeyExists( sessions[ application.applicationName ], arguments.cfid );
	}

	function getHttpSessionId() {
		var httpSession = getPageContext().getRequest().getSession( false );
		return isNull( httpSession ) ? "" : httpSession.getId();
	}

	session.user_id = createUUID();
	oldSessionId = session.sessionid;
	oldHttpSessionId = getHttpSessionId();
	oldCfid = getPageContext().getCFID();

	sessionInvalidate();

	newCfid = getPageContext().getCFID();
	result = {
		oldSessionId: oldSessionId,
		oldHttpSessionId: oldHttpSessionId,
		oldSessionExists: hasSessionScope( oldCfid ),
		cfidChanged: oldCfid != newCfid,
		replacementCreatedEagerly: len( getHttpSessionId() ) > 0 || hasSessionScope( newCfid )
	};

	result.newSessionId = session.sessionid;
	result.newHttpSessionId = getHttpSessionId();
	result.hasUserId = structKeyExists( session, "user_id" );
	result.sessionCreatedOnAccess = len( result.newHttpSessionId ) > 0 && hasSessionScope( newCfid );

	echo( serializeJSON( result ) );
</cfscript>
