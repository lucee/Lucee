<cfscript>
	param name="form.action" default="createSession";

	switch ( form.action ) {
		case "createSession":
			// Trigger session creation and write
			session.testValue = "LDEV5964-#now()#";
			session.counter = ( session.counter ?: 0 ) + 1;

			// Force session to persist by modifying it
			writeOutput( "success: session created with counter=#session.counter#" );
			break;

		default:
			writeOutput( "unknown action: #form.action#" );
	}
</cfscript>
