<cfcomponent>
	<cffunction name="test" access="public" output="false" returntype="boolean">
		<cfreturn foo()>
	</cffunction>

	<cffunction name="foo" access="public" output="false" returntype="boolean">
		<cfreturn true>
	</cffunction>
</cfcomponent>