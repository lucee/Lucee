<cfscript>
// LDEV-6078: Else block with first statement
// Bug: } else { and first statement inside share same label

function checkValue( required numeric value ) {
	var result = "";
	if ( arguments.value > 100 ) {
		result = "high";
	} else if ( arguments.value > 50 ) {
		result = "medium";
	} else {
		result = "low";
	}
	return result;
}

systemOutput( checkValue( 25 ), true );
</cfscript>
