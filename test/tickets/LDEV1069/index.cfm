<cfsetting enablecfoutputonly="true">
<cftry>
	<cfif structCount(FORM) GT 0 >
		<cfset structKeyTranslate(FORM) />
	</cfif>
	<cfcatch type="any">
		<cfoutput>#cfcatch.Message#</cfoutput>
	</cfcatch>
</cftry>