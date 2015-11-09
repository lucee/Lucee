<cfif !isNull(url.readonly)>
	<cfapplication cgiReadOnly="#url.readonly#">
</cfif>