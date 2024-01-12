
<cfscript>
	param name="form.action";

	//systemOutput("LDEV-4670 #form.action#", true);

	settings = getApplicationSettings();
	params = {
		name: settings.name
	};

	//systemOutput( params, true );

	switch ( form.action ){
		case "purge":
			query name="purge_sessions" result="deleted" params=params {
				echo("DELETE FROM cf_session_data WHERE name = :name");
			}
			echo( deleted.toJson() );
			break;
		case "dumpDatabaseSessions":
			query name="q_sessions" params=params {
				echo("SELECT expires, name, cfid FROM cf_session_data WHERE name = :name");
			}
			echo( q_sessions.toJson() );
			break;
		case "dumpMemorySessions":
			sess = getPageContext().getCFMLFactory().getScopeContext().getAllCFSessionScopes();
			//systemOutput("getAllCFSessionScopes()", true);
			//systemOutput(sess, true);
			if ( structKeyExists( sess, settings.name ) ) {
				st = sess[ settings.name ];
				keys = structKeyArray( st );
				echo( queryNew( structKeyList( st[ keys[ 1 ] ] ) ,"" , st ).toJson() );
			} else {
				echo( queryNew( "cfid,expires" ).toJson() );
			}
			break;
		case "createSession":
			echo("['sessionCreated: #session.cfid#']");
			break;
		case "checkSession":
			echo( session.toJson() );
			break;
		default:
			throw "unknown action [#form.action#]";
	}
</cfscript>
	