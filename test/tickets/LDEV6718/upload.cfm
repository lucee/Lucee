<cfscript>
	cffile( action="upload", filefield="myFile", destination=url.destination, nameconflict=url.nameconflict, result="r" );
	result = {
		"serverFileExtExists": structKeyExists( r, "serverFileExt" ),
		"serverFileExt": r.serverFileExt,
		"clientFileExt": r.clientFileExt,
		"serverFile": r.serverFile,
		"saved": fileExists( r.serverDirectory & "/" & r.serverFile )
	};
	content reset="true" type="application/json; charset=utf-8";
	writeOutput( serializeJSON( result ) );
</cfscript>
