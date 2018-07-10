<cfset obj = new child() />
<cfset obj.getparentProp />
<cfset obj.getchildProp />
<cfset sJson = serializeJSON( obj ) >
<cfoutput>
	#sJson#
</cfoutput>
