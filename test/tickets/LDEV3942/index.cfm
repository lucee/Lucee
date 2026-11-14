<cfscript>
	// cflogin attributes are passed via url so each test can pick its own combination
	attrs = {};
	if ( structKeyExists( url, "applicationToken" ) ) attrs.applicationToken = url.applicationToken;
	if ( structKeyExists( url, "idleTimeout" ) ) attrs.idleTimeout = url.idleTimeout;
	if ( structKeyExists( url, "cookieDomain" ) ) attrs.cookieDomain = url.cookieDomain;
</cfscript>
<cflogin attributeCollection="#attrs#">
	<cfloginuser name="test" password="password" roles="user,admin">
</cflogin>
<cfscript>
	authKeys = [];
	loop collection="#session#" key="k" {
		if ( left( k, 16 ) == "cfauthorization_" ) authKeys.append( uCase( k ) );
	}
	echo( serializeJSON( {
		"authKeys": authKeys,
		"loggedIn": getAuthUser() == "test",
		"isUser": isUserInRole( "user" ),
		"isAdmin": isUserInRole( "admin" )
	} ) );
</cfscript>
