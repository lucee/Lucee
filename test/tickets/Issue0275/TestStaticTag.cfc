<cfcomponent>
	<cfstatic>
		<cfset static1=1>
	</cfstatic>

	<cfstatic>
		<cfset static2=2>
	</cfstatic>

	<cfscript>
	static { 
		static3=3;
	}
	</cfscript>
	<cfscript>
	static { 
		static4=4;
	}
	</cfscript>
	
	<cffunction name="getTheStaticScope2" modifier="static">
		<cfreturn static>
	</cffunction>
	<cfscript> 
	static function getTheStaticScope(){
		return static; 
	}

	function getTheStaticScopeThis(){
		return static;
	}
</cfscript>
</cfcomponent>