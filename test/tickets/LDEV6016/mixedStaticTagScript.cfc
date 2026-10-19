<cfcomponent>
	<cfscript>
	static {
		static1=1;
	}
	</cfscript>

	<cffunction name="getTheStaticScope" modifier="static">
		<cfreturn static>
	</cffunction>
</cfcomponent>
