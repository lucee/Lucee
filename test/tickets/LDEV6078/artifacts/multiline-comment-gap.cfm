<cfscript>
// LDEV-6078: Multiline comment between statements
// Bug: statement before and after multiline comment share same label

function process( required string input ) {
	var local = structNew();
	/*
		This is a multiline comment
		that spans several lines
		and may cause line number issues
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
