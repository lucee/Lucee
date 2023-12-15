
<cfscript>
	param name="form.action" default="";

	//systemOutput("LDEV-4670 #form.action#", true);

	settings = getApplicationSettings();
	params = {
		name: settings.name
	};

	//systemOutput( params, true );

	switch (form.action){
		case "purge":
			query name="purge_sessions" result="deleted" params=params {
				echo("DELETE FROM cf_session_data WHERE name = :name");
			}
			echo( deleted.toJson() );
			break;
		case "dump":
			query name="q_sessions" params=params {
				echo("SELECT expires, name, cfid FROM cf_session_data WHERE name = :name");
			}
			echo( q_sessions.toJson() );
			break;
		default:
			echo( session.toJson() );
	}
</cfscript>
	