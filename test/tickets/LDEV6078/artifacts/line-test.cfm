<cfscript>
// Test file for LDEV-6078: SourceCode.getPosition() line number bug
// Each statement should have its own unique line number in the execution log

function testFunction( required numeric value ) {
	var result = 0;
	if ( arguments.value > 10 ) {
		result = arguments.value * 2;
	}
	return result;
}

// Execute the function
echo( testFunction( 15 ) );
</cfscript>
