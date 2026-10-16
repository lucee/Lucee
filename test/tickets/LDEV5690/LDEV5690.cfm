<cfscript>
// Test simple variable in brackets - Brad Wood's example from LDEV-5690
try {
	brad = "testKey";
	foo = {};
	param name="foo[ brad ]" default="bar";

	if ( structKeyExists( foo, "testKey" ) && foo.testKey == "bar" ) {
		writeOutput( "SUCCESS" );
	} else {
		writeOutput( "FAILED: foo.testKey = #foo.testKey ?: 'undefined'#" );
	}
} catch( any e ) {
	writeOutput( "FAILED: #e.stacktrace#" );
}
</cfscript>
