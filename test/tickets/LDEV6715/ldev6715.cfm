<cfscript>
	param name="url.action";
	// a version that is never in the local Maven store, so a download has to happen
	version = "0.0.#getTickCount()##randRange( 1000, 9999 )#";
	error = "";
	try {
		if ( url.action == "load" ) {
			mavenLoad( [ "org.lucee.test:ldev6715:#version#" ] );
		}
		else if ( url.action == "import" ) {
			// the dependency is resolved on a worker thread
			pomPath = getTempDirectory() & "ldev6715-" & createUUID() & ".xml";
			fileWrite( pomPath, '<?xml version="1.0" encoding="UTF-8"?>'
				& '<project xmlns="http://maven.apache.org/POM/4.0.0">'
				& '<modelVersion>4.0.0</modelVersion>'
				& '<groupId>org.lucee.test</groupId><artifactId>ldev6715-parent</artifactId><version>1.0.0</version>'
				& '<dependencies>'
				& '<dependency><groupId>org.lucee.test</groupId><artifactId>ldev6715-dep</artifactId><version>#version#</version></dependency>'
				& '</dependencies>'
				& '</project>' );
			try {
				mavenImport( pomPath, true );
			}
			finally {
				fileDelete( pomPath );
			}
		}
		else {
			throw "unknown action [#url.action#]";
		}
	}
	catch ( any e ) {
		// the connect failure can be in the cause, the stacktrace has the whole chain
		error = e.message & chr( 10 ) & e.stacktrace;
	}
	writeOutput( len( error ) ? error : "no error, the artifact [org.lucee.test:ldev6715:#version#] should not exist" );
</cfscript>
