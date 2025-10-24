<cfscript>
	o = createObject( "component", "forCreateobject" );
	writeOutput( structKeyExists( o, "setupCalled" ) ? "FAIL" : "PASS" );
</cfscript>
