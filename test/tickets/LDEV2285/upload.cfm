<cfscript>
	try {
		if ( url.mode == "tag" ) {
			cffile( action="upload", filefield="myFile", destination=url.destination, nameconflict="error", result="r" );
			results = [ r ];
		}
		else if ( url.mode == "function" ) {
			results = [ fileUpload( url.destination, "myFile", "", "error" ) ];
		}
		else if ( url.mode == "uploadAll" ) {
			cffile( action="uploadAll", destination=url.destination, nameconflict="error", result="r" );
			results = r;
		}
		else {
			results = fileUploadAll( url.destination, "", "error" );
		}
		out = [];
		for ( r in results ) {
			arrayAppend( out, {
				"clientFile": r.clientFile,
				"clientFileName": r.clientFileName,
				"clientFileExt": r.clientFileExt,
				"clientDirectory": r.clientDirectory,
				"serverFile": r.serverFile,
				"saved": fileExists( r.serverDirectory & "/" & r.serverFile ),
				"size": fileExists( r.serverDirectory & "/" & r.serverFile ) ? getFileInfo( r.serverDirectory & "/" & r.serverFile ).size : -1
			} );
		}
		result = { "results": out };
	}
	catch ( any e ) {
		result = { "error": e.message };
	}
	content reset="true" type="application/json; charset=utf-8";
	// url encoded, so the response stays ASCII
	writeOutput( urlEncodedFormat( serializeJSON( result ) ) );
</cfscript>
