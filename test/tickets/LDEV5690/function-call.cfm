<cfscript>
// Test that function calls are still blocked with limitEvaluation=true
try {
	foo = {};
	param name="foo[ now() ]" default="bar";

	//  If we get here, the param succeeded which means the exploit still works
	writeOutput( "FAILED: function call should have been blocked" );
} catch( any e ) {
	// Any exception is good - means the function call was blocked
	writeOutput( "SUCCESS" );
}
</cfscript>
