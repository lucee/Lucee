<cfinterface>
	<cffunction name="required" returntype="string" access="public">
	</cffunction>
	<cffunction name="tagDefault" returntype="string" access="public" modifier="default">
		<cfargument name="name" type="string" required="true">
		<cfreturn "Tag " & arguments.name>
	</cffunction>
</cfinterface>
