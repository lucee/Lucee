<cfparam name="FORM.Scene" default="1">
<cfif FORM.Scene EQ 1>
	<cfset SampleCSRFToken = "Test">
	<cfoutput>#CSRFverifyToken(SampleCSRFToken)#</cfoutput>
<cfelseif FORM.Scene EQ 2>
	<cfset SampleCSRFToken = CSRFGenerateToken()>
	<cfoutput>#CSRFverifyToken(SampleCSRFToken)#</cfoutput>
<cfelse>
	<cfset SampleCSRFToken = CSRFGenerateToken("MyKey")>
	<cfoutput>#CSRFverifyToken(SampleCSRFToken, "MyKey")#</cfoutput>
</cfif>