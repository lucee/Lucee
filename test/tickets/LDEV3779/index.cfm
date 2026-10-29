<cfscript>
	if ( url.mode == "override" ) {
		http url=url.target result="local.rsp" listener={
			before: function( args, caller ) {
				arrayAppend( args.params, { type: "header", name: "X-Listener", value: "tag" } );
				return arguments;
			}
		};
	}
	else if ( url.mode == "cfapplication" ) {
		application action="update" httpListener={
			after: function( args, caller, result ) {
				result.fileContent &= "|cfapplication-after";
				return { result: result };
			}
		};
		http url=url.target result="local.rsp";
	}
	else {
		http url=url.target result="local.rsp";
	}
	writeOutput( local.rsp.fileContent );
</cfscript>
