<cfscript>
	param name="url.dumpEndedSessions" default="false";
	if ( url.dumpEndedSessions ){
		echo( structKeyList( application.endedSessions) );
	} else {
		session.luceeRocks=true;
		echo( session.sessionid );
	}
</cfscript>