<cfscript>
try {
	if ( url.target == "cfm" ) include "/ldev6375/ghost.cfm";
	else createObject( "component", "ldev6375.ghost" );
	echo( "FOUND" );
}
catch ( any e ) {
	echo( "MISSING" );
}
</cfscript>
