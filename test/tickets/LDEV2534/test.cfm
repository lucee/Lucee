<cftry>
	<cfset testObj = createobject("component","test")>
	<cfset res = testObj.callFunction()>
	<cfoutput>#res#</cfoutput>
<cfcatch name="e">
	<cfoutput>Routines cannot be declared more than once.</cfoutput>
</cfcatch>
</cftry>