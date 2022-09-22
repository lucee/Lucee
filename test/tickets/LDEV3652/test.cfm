<cfset obj = new object() />
<cfset obj.setNumber1(10) />
<cfset obj.setNumber2(20) />
<cfset obj.setNumber3(30) />
<cfset sJson = serializeJSON( obj ) >
<cfoutput>
	#sJson#
</cfoutput>
