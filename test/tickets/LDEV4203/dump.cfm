<cfscript>
	pc = getPageContext();
	req = pc.getRequest();
	result = {
		"webCharset": pc.getWebCharset().name(),
		"resourceCharset": pc.getResourceCharset().name(),
		"requestEncoding": isNull( req.getCharacterEncoding() ) ? "" : req.getCharacterEncoding(),
		"urlValue": url.keyExists( "u" ) ? url.u : "",
		"formValue": form.keyExists( "f" ) ? form.f : "",
		"body": getHTTPRequestData().content
	};
	header name="Content-Type" value="text/plain; charset=utf-8";
	writeOutput( serializeJSON( result ) );
</cfscript>
