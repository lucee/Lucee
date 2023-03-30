<cfparam name="form.email">
<cftry>
	<!--- will throw missing / invalid from address --->
	<cfmail from="#form.email#" to="#form.email#" subject="mail test">
		Dummy email
	</cfmail>
	<cfcatch>
		<cfoutput>#cfcatch.message#</cfoutput>
		<cfabort>
	</cfcatch>
</cftry>
<cfoutput>ok</cfoutput>