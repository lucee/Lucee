<cfscript>
	echo( "[BEFORE]" );
	cfflush();
	echo( "[COMMITTED:" & getPageContext().getHttpServletResponse().isCommitted() & "]" );
	header name="X-LDEV3653" value="late";
	echo( "[AFTER]" );
</cfscript>
