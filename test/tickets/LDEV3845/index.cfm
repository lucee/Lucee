<cfparam name="form.email">
<cfparam name="form.charset">
<cfparam name="form.subject">
<cftry>
	<!--- will throw missing / invalid from address --->
	<cfmail from="#form.email#" to="#form.email#" subject="#form.subject#" charset="#form.charset#" async="false" debug=true>
		Dummy email
	</cfmail>
	<cfcatch>
		<cfoutput>#cfcatch.message#</cfoutput>
		<cfabort>
	</cfcatch>
</cftry>
<cfoutput>ok</cfoutput>