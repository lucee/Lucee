<cfcomponent localmode="modern">
	<cffunction name="probe" localmode="classic">
		<cfset unscoped = 1>
		<cfreturn { "variables": structKeyExists( variables, "unscoped" ), "local": structKeyExists( local, "unscoped" ) }>
	</cffunction>
</cfcomponent>
