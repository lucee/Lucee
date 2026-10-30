<cfscript>
	writeOutput( "a    b" );
	flush;
	setting whitespaceManagement=url.mode;
	writeOutput( "c    d" );
</cfscript>
