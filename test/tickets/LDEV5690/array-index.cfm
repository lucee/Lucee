<cfscript>
// Test array index access in brackets
try {
	keys = [ "first", "second" ];
	data = {};
	param name="data[ keys[1] ]" default="value1";

	if ( structKeyExists( data, "first" ) && data.first == "value1" ) {
		writeOutput( "SUCCESS" );
	} else {
		writeOutput( "FAILED: expected key 'first' with value 'value1' not found" );
	}
} catch( any e ) {
	writeOutput( "FAILED: #e.stacktrace#" );
}
</cfscript>
