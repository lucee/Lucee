<cfscript>
	setting enablecfoutputonly = true;
	if ( structKeyExists( url, "clear" ) ) structClear( session );
	if ( structKeyExists( url, "token" ) ) {
		writeOutput( serializeJSON( { token: csrfGenerateToken( "ldevxxxx" ), cfid: cookie.cfid, cftoken: cookie.cftoken } ) );
	}
	else if ( structKeyExists( url, "verify" ) ) {
		writeOutput( csrfVerifyToken( url.verify, "ldevxxxx" ) ? "valid" : "invalid" );
	}
	else {
		writeOutput( serializeJSON( { cfid: cookie.cfid, cftoken: cookie.cftoken } ) );
	}
</cfscript>
