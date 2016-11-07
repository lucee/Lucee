<cfparam name="FORM.Scene" default="1">
<cfif FORM.Scene EQ 1>
	<cfset objCSRF1 = cSRFGenerateToken(forceNew=true)>
	<cfset objCSRF2 = cSRFGenerateToken(forceNew=true)>
<cfelse>
	<cfset objCSRF1 = cSRFGenerateToken(forceNew=false)>
	<cfset objCSRF2 = cSRFGenerateToken(forceNew=false)>
</cfif>
<cfoutput>#objCSRF1 EQ objCSRF2#</cfoutput>