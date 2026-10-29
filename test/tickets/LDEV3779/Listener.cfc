component {

	function before( args, caller ) {
		arrayAppend( args.params, { type: "header", name: "X-Listener", value: "component" } );
		args.url = args.url & "component";
		return arguments;
	}

	function after( args, caller, result ) {
		result.fileContent = "[" & result.fileContent & "]";
		return { result: result };
	}

	function error( args, caller, exception ) {
		return { result: { fileContent: "handled:" & exception.message, statusCode: "200 handled" } };
	}

}
