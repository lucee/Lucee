component {

	this.name = "LDEV3779-" & hash( getCurrentTemplatePath() );

	this.http.listener = {
		before: function( args, caller ) {
			arrayAppend( args.params, { type: "header", name: "X-Listener", value: "application" } );
			return arguments;
		},
		after: function( args, caller, result ) {
			result.fileContent &= "|application-after";
			return { result: result };
		}
	};

}
