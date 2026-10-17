<cfscript>
	setting enablecfoutputonly = true;
	if ( structKeyExists( url, "clear" ) ) structClear( session );
	if ( structKeyExists( url, "write" ) ) session.box.v = url.write;
	if ( structKeyExists( url, "token" ) ) {
		writeOutput( serializeJSON( { token: csrfGenerateToken( "ldev6596" ), cfid: cookie.cfid, cftoken: cookie.cftoken } ) );
	}
	else if ( structKeyExists( url, "verify" ) ) {
		writeOutput( csrfVerifyToken( url.verify, "ldev6596" ) ? "valid" : "invalid" );
	}
	else if ( structKeyExists( url, "read" ) ) {
		writeOutput( ( session.box.v ?: "" ) );
	}
	else {
		writeOutput( serializeJSON( { cfid: cookie.cfid, cftoken: cookie.cftoken } ) );
	}
</cfscript>
