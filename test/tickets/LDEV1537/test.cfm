<cftry>
	<!--- will throw missing / invalid from address --->
	<cfmail from="" to="xxx@yy.com" subject="mail test">
		Dummy email
	</cfmail>
	<cfcatch>
		<cfoutput>#cfcatch.message#</cfoutput>
		<cfabort>
	</cfcatch>
</cftry>
<cfoutput>ok</cfoutput>