<cftry>
	<cfset hasError = true>
	<cfset testObj = new test(alice = 'james', bob = true)>

	<cfcatch>
		<cfset hasError = false>
	</cfcatch>
</cftry>
<cfoutput>#hasError#</cfoutput>
