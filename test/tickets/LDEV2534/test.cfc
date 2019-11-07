<cfcomponent>
	<cffunction name="callFunction">
		<cfset data = structkeyarray(server.os)>
		<cfreturn data>
	</cffunction>

	<cffunction name="callFunction">
		<cfset data = server.os>
		<cfreturn data>
	</cffunction>

	<cffunction name="callFunction">
		<cfset data = server.os.name>
		<cfreturn data>
	</cffunction>
</cfcomponent>