<cfcomponent>
	<cfproperty name="id">

	<cffunction name="getVersion" modifier="static" returntype="string">
		<cfreturn "1.0">
	</cffunction>

	<cffunction name="init">
		<cfreturn this>
	</cffunction>
</cfcomponent>
