<cfscript>
	setting showdebugoutput=false;
	out = { "exception": "" };
	args = {
		destination: url.destination,
		nameconflict: url.nameconflict,
		allowedExtensions: "txt"
	};
	if ( structKeyExists( url, "continueOnError" ) ) args.continueOnError = url.continueOnError;
	if ( structKeyExists( url, "errors" ) ) args.errors = url.errors;
	try {
		switch ( url.mode ) {
			case "tagAll":
				args.action = "uploadAll";
				args.result = "r";
				cffile( attributeCollection=args );
				out.result = r;
				break;
			case "tagAllDefault":
				args.action = "uploadAll";
				cffile( attributeCollection=args );
				out.result = cffile;
				break;
			case "tagOne":
				args.action = "upload";
				args.filefield = url.field;
				args.result = "r";
				cffile( attributeCollection=args );
				out.result = r;
				break;
			case "funcAll":
				if ( structKeyExists( url, "continueOnError" ) && structKeyExists( url, "errors" ) ) {
					out.result = fileUploadAll( destination=url.destination, nameConflict=url.nameconflict, allowedExtensions="txt", continueOnError=url.continueOnError, errors=url.errors );
				}
				else if ( structKeyExists( url, "continueOnError" ) ) {
					out.result = fileUploadAll( destination=url.destination, nameConflict=url.nameconflict, allowedExtensions="txt", continueOnError=url.continueOnError );
				}
				else {
					out.result = fileUploadAll( destination=url.destination, nameConflict=url.nameconflict, allowedExtensions="txt" );
				}
				break;
			case "funcOne":
				if ( structKeyExists( url, "continueOnError" ) ) {
					out.result = fileUpload( destination=url.destination, fileField=url.field, nameConflict=url.nameconflict, allowedExtensions="txt", continueOnError=url.continueOnError );
				}
				else {
					out.result = fileUpload( destination=url.destination, fileField=url.field, nameConflict=url.nameconflict, allowedExtensions="txt" );
				}
				break;
		}
		if ( structKeyExists( url, "errors" ) && url.mode != "funcOne" && structKeyExists( variables, url.errors ) ) out.errors = variables[ url.errors ];
	}
	catch ( any e ) {
		out.exception = e.message;
	}
	content reset="true" type="application/json; charset=utf-8";
	echo( serializeJSON( out ) );
</cfscript>
