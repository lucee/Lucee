<cfset obj = StructNew() />
<cfset obj.prop = "prop" />
<cfset sJson = serializeJSON( obj ) >
<cfoutput>
	#sJson#
</cfoutput>
