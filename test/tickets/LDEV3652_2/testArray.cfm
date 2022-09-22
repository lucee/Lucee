<cfset obj = ArrayNew() />
<cfset obj[1] = "prop" />
<cfset obj[2] = 20 />
<cfset obj[3] = true />
<cfset sJson = serializeJSON( obj ) >
<cfoutput>
	#sJson#
</cfoutput>
