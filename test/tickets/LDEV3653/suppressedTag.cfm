<cfoutput>[BEFORE]</cfoutput>
<cfflush>
<cfoutput>[COMMITTED:#getPageContext().getHttpServletResponse().isCommitted()#]</cfoutput>
<cfheader name="X-LDEV3653" value="late" throwOnError="false">
<cfoutput>[AFTER:#1 + 1#]</cfoutput>
