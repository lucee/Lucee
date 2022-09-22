<cfset obj = StructNew() />
<cfset obj.nested = StructNew() />
<cfset obj.nested.prop = "nestedProp" />
<cfset sJson = serializeJSON( obj ) >
<cfoutput>
	#sJson#
</cfoutput>
