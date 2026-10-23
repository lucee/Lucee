<cfscript>
	result = { "fields": {}, "files": {} };
	for ( k in form ) {
		if ( k == "fieldnames" || listFindNoCase( url.files ?: "", k ) ) continue;
		result.fields[ k ] = form[ k ];
	}
	for ( ff in listToArray( url.files ?: "" ) ) {
		cffile( action="upload", filefield=ff, destination=getTempDirectory(), nameconflict="makeunique", result="r" );
		path = r.serverDirectory & "/" & r.serverFile;
		result.files[ ff ] = {
			"clientFile": r.clientFile,
			"size": r.fileSize,
			"contentType": r.contentType & "/" & r.contentSubType,
			"hash": hash( fileReadBinary( path ), "MD5" )
		};
		fileDelete( path );
	}
	content reset="true" type="application/json; charset=utf-8";
	writeOutput( serializeJSON( result ) );
</cfscript>
