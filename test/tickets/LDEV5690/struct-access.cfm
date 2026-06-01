<cfscript>
// Test struct member access - ColdBox 7 use case
try {
	mConfig = { modelNamespace: "mymodels" };
	globalModuleSettings = {};
	param name="globalModuleSettings[ mConfig.modelNamespace ]" default="";

	if ( structKeyExists( globalModuleSettings, "mymodels" ) ) {
		writeOutput( "SUCCESS" );
	} else {
		writeOutput( "FAILED: expected key 'mymodels' not found" );
	}
} catch( any e ) {
	writeOutput( "FAILED: #e.stacktrace#" );
}
</cfscript>
