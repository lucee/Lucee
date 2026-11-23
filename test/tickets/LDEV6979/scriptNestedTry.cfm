<cfscript>
request.ldev6979 = [];
try {
	arrayAppend( request.ldev6979, "try" );
	if ( request.ldev6979Throw ) throw "LDEV-6979 try";
}
catch ( any e ) {
	arrayAppend( request.ldev6979, "catch" );
}
finally {
	if ( true ) {
		z = 0; try { arrayAppend( request.ldev6979, "inner" ); } catch ( any e2 ) {}
	}
}
</cfscript>
