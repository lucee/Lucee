<cfscript>
// LDEV-6078: Multiline comment between statements (with real bytecode)
// This tests multiline comments with actual bytecode-generating statements

function process( required string input ) {
	var data = structNew();
	/*
		This is a multiline comment
		that spans several lines
		and should NOT cause line number issues
		because both statements generate bytecode
	*/
	if ( len( input ) > 0 ) {
		local.result = input;
	} else {
		local.result = "empty";
	}
	return local.result;
}

systemOutput( process( "test" ), true );
</cfscript>
