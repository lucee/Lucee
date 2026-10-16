<cfscript>
// LDEV-6078: Comment before elseif
// Bug: closing brace and elseif after comment share same label

function classify( required string input ) {
	var result = "";
	if ( input == "a" ) {
		result = "alpha";
	}
	// Check for beta
	elseif ( input == "b" ) {
		result = "beta";
	}
	// Check for gamma
	elseif ( input == "c" ) {
		result = "gamma";
	}
	else {
		result = "unknown";
	}
	return result;
}

systemOutput( classify( "b" ), true );
</cfscript>
