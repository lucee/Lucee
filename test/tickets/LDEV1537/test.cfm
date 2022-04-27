<cftry>
	<cfset hasError = false>
    <cfmail from="" to="xxx@yy.com" subject="mail test">
    	Dummy email
    </cfmail>
	<cfcatch>
		<cfset hasError = true>
	</cfcatch>
</cftry>
<cfoutput>#hasError#</cfoutput>