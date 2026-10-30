<cfcomponent>
	<cffunction name="echo" returntype="string" output="false">
		<cfargument name="data" type="json" required="true">
		<cfreturn arguments.data>
	</cffunction>

	<cffunction name="optional" returntype="string" output="false">
		<cfargument name="data" type="json" default="{}">
		<cfreturn arguments.data>
	</cffunction>

	<cffunction name="badReturn" returntype="json" output="false">
		<cfargument name="data" type="any" required="true">
		<cfreturn arguments.data>
	</cffunction>
</cfcomponent>
