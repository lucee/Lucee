<cfquery name="result">
	select * from test
</cfquery>
<cfoutput>#result.RECORDCOUNT#</cfoutput>