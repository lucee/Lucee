<cfcache action="get"  id="cache" name="cachedVar">
</cfcache>
<cfif isNull(cachedVar)>
	<cfcache action="put" value="#APPLICATION.ApplicationName#" id="cache">
	</cfcache>
</cfif>
<cfcache action="get"  id="cache" name="cachedVar">
</cfcache>
<cfoutput>#cachedVar#</cfoutput>
