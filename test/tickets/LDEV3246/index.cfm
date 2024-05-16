<cftry>

	<cfparam name="FORM.myField" default="#nullValue()#" type="any">
	<cfoutput>#FORM.myField#</cfoutput>

	<cfcatch type="any">
		<cfoutput>#cfcatch.message#</cfoutput>
	</cfcatch>

</cftry>