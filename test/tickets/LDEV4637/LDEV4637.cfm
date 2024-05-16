<cfscript>
	param name="url.type";
	param name="form.resource" default="s3:///";

	try {
		switch ( url.type ){
			case "dir":
				echo( directoryExists( form.resource ) );
				break;
			case "file":
				echo( fileExists( form.resource ) );
				break;
			default:
				// meh
		}
	} catch ( e ) {
		throw listFirst( replaceNoCase( e.stacktrace, getApplicationSettings().s3.awsSecretKey, "***", "all" ), "." );
	}
</cfscript>