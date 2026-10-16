<cfscript>
	setting enablecfoutputonly = true;
	if ( structKeyExists( url, "clear" ) ) structClear( session );
	if ( structKeyExists( url, "token" ) ) {
		writeOutput( serializeJSON( { token: csrfGenerateToken( "ldev6594" ), cfid: cookie.cfid, cftoken: cookie.cftoken } ) );
	}
	else if ( structKeyExists( url, "verify" ) ) {
		writeOutput( csrfVerifyToken( url.verify, "ldev6594" ) ? "valid" : "invalid" );
	}
	else {
		writeOutput( serializeJSON( { cfid: cookie.cfid, cftoken: cookie.cftoken } ) );
	}
</cfscript>
