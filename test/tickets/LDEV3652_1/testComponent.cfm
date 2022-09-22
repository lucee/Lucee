<cfset obj = new objects.objectComponent() />
<cfset obj.setName("Test Name") />
<cfset obj.setAge(20) />
<cfset obj.setBirthDate(CreateDate(1990,11,20)) />
<cfset obj.setIsCurrent(false) />
<cfset sJson = serializeJSON( obj ) >
<cfoutput>
	#sJson#
</cfoutput>
