<cfscript>
// LDEV-6078: Closing brace then blank/comment then if
// Bug: } line and if line after blank/comment share same label

function processData( required string path ) {
	var result = "";

	if ( len( path ) > 0 ) {
		result = "has path";
	}

	// Check if directory exists
	if ( directoryExists( path ) ) {
		result &= " - exists";
	}

	/*
		Multiline comment here
		before next statement
	*/
	if ( fileExists( path ) ) {
		result &= " - is file";
	}

	return result;
}

systemOutput( processData( "/" ), true );
</cfscript>
