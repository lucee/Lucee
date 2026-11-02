<cfscript>
	param name="url.token";
	param name="url.log";
	writeLog( text="testing #url.log# #url.token#", log=url.log, type="information" );
	// the resource appender is asynchronous
	sleep( 500 );
</cfscript>
