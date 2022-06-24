<cftry>
	<!--- will throw invalid to address --->
	<cfmail from="aaa@bb.com" to="to:81@gmail.com" subject="sample">dummy email</cfmail>
	<cfcatch>
		<cfoutput>#cfcatch.message#</cfoutput>
		<cfabort>
	</cfcatch>
</cftry>
<cfoutput>ok</cfoutput>