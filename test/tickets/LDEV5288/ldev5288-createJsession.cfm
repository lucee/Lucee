<cfscript>
	session.ldev5288 = true;
	session.counter = ( session.counter ?: 0 ) + 1;
	echo( "session:" & session.toJson() );
	echo( chr(10) );
	echo( "cookie: " & cookie.toJson() );
	echo( chr(10) );
</cfscript>
