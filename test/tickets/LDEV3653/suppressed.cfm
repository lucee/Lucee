<cfscript>
	echo( "[BEFORE]" );
	cfflush();
	echo( "[COMMITTED:" & getPageContext().getHttpServletResponse().isCommitted() & "]" );
	header name="X-LDEV3653" value="late" throwOnError=false;
	header statusCode=404 statusText="Not Found" throwOnError=false;
	echo( "[AFTER:" & ( 1 + 1 ) & "]" );
</cfscript>
