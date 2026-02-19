<cfcomponent hint="Tag-based component hint">

	<cffunction name="withHint" access="public" returntype="string">
		<cfargument name="name" type="string" required="true" hint="The user's full name">
		<cfreturn arguments.name>
	</cffunction>

	<cffunction name="withDefault" access="public" returntype="string">
		<cfargument name="greeting" type="string" required="false" default="Hello">
		<cfreturn arguments.greeting>
	</cffunction>

	<cffunction name="withBoth" access="public" returntype="string">
		<cfargument name="message" type="string" required="true" hint="The message to display" default="Welcome">
		<cfreturn arguments.message>
	</cffunction>

	<cffunction name="withFuncHint" access="public" returntype="string" hint="Function hint from attribute">
		<cfreturn "hint">
	</cffunction>

	<cffunction name="remoteFunc" access="remote" returntype="struct" returnformat="json">
		<cfreturn {"status": "ok"}>
	</cffunction>

</cfcomponent>
