<cfset obj = new objects.objectWithCustomGetter() />
<cfset obj.setName("Test Name") />
<cfset obj.setPassword("testPassword1234") />
<cfset sJson = serializeJSON( obj ) >
<cfoutput>
	#sJson#
</cfoutput>
