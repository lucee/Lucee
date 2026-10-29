<cfscript>
	echo( "[COMMITTED:" & getPageContext().getHttpServletResponse().isCommitted() & "]" );
	header name="X-LDEV3653" value="early" throwOnError=false;
	echo( "[AFTER:" & ( 1 + 1 ) & "]" );
</cfscript>
