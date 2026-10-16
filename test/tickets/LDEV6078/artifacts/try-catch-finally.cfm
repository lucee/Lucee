<cfscript>
// LDEV-6078: Try-catch-finally block transitions

function safeDivide( required numeric a, required numeric b ) {
	var result = 0;
	try {
		result = a / b;
	} catch ( any e ) {
		result = -1;
	} finally {
		systemOutput( "Division attempted", true );
	}
	return result;
}

systemOutput( safeDivide( 10, 2 ), true );
</cfscript>
