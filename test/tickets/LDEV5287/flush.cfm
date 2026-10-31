<cfscript>
	cookie.before = 1;
	cookie.other = "x";
	cookie.before = 2;
	echo( "first" );
	cfflush();
	echo( "second" );
</cfscript>
