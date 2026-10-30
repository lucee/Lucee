<cfcomponent localmode="modern">
	<cffunction name="probe">
		<cfset unscoped = 1>
		<cfreturn { "variables": structKeyExists( variables, "unscoped" ), "local": structKeyExists( local, "unscoped" ) }>
	</cffunction>
</cfcomponent>
