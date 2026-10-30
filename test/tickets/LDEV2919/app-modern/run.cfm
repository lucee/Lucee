<cfscript>
	param name="url.cfc";
	param name="url.fn" default="probe";
	try {
		c = createObject( "component", "ldev2919.#url.cfc#" );
		r = invoke( c, url.fn );
		writeOutput( "variables=" & r.variables & ";local=" & r.local );
	}
	catch ( any e ) {
		writeOutput( "error=" & e.message );
	}
</cfscript>
