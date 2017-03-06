<cfparam name="FORM.Scene" default="1">
<cfset hasError = false>
<cfset errorMsg = "">
<cftry>
	<cfif FORM.Scene EQ 1>
		<cfset o = CreateObject( 'component', 'test1')>
	<cfelse>
		<cfset o = CreateObject( 'component', 'test2')>
	</cfif>
	<cfcatch type="any">
		<cfset hasError = true>
		<cfset errorMsg = cfcatch.Message>
		<!--- <cfdump var="#cfcatch#" /> --->
	</cfcatch>
</cftry>
<cfoutput>#hasError#|#errorMsg#</cfoutput>