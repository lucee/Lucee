<cfscript>
	key = "cfml_cache_ldev3224";
	token = csrfGenerateToken( key );
	if ( !csrfVerifyToken( token, key ) )
		throw "invalid CSRF token before sessionRotate";
	sessionRotate();
	if ( !csrfVerifyToken( token, key ) )
		throw "invalid CSRF token after sessionRotate";
	echo( "all good" );
</cfscript>
