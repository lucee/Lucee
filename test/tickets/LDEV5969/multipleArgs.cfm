<cffunction name="greet" access="remote" returntype="string">
	<cfargument name="firstName" type="string" required="true">
	<cfargument name="lastName" type="string" required="false" default="">
	<cfreturn arguments.firstName & " " & arguments.lastName>
</cffunction>
