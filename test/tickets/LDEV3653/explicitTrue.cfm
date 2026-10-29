<cfscript>
	echo( "[BEFORE]" );
	cfflush();
	echo( "[COMMITTED:" & getPageContext().getHttpServletResponse().isCommitted() & "]" );
	header name="X-LDEV3653" value="late" throwOnError=true;
	echo( "[AFTER:" & ( 1 + 1 ) & "]" );
</cfscript>
