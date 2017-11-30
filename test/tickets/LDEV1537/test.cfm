<cftry>
	<cfset hasError = false>
    <cfmail from="" to="xxx@yy.com" subject="mail test" server="localhost">
    	Dummy email
    </cfmail>
	<cfcatch>
		<cfset hasError = true>
	</cfcatch>
</cftry>
<cfoutput>#hasError#</cfoutput>