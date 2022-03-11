<cfscript>
	param name="url.dumpEndedSessions" default="false";
	if ( url.dumpEndedSessions ){
		param name="url.check";
		systemOutput("", true);
		systemOutput( structKeyList(server.LDEV3264_endedSessions), true);
		systemOutput("", true);
		echo( structKeyExists(server.LDEV3264_endedSessions, url.check ) );
	} else {
		session.luceeRocks=true;
		echo( session.sessionid );
		applicationStop();
	}
</cfscript>