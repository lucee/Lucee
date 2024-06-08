<cfcomponent>
	<cffunction name="testTag">
		<cfargument name="a" default="udf">
		<cfset local.foo = function(a="argclosuretag"){return "closure-insidetag:"&arguments.a;}> <!---fine--->
		<cfreturn "tag:"&foo()>
	</cffunction>
	
	<cfscript>echo("constr");
		function testScript() {
			local.foo = function(a="argclosurescript"){return "closure-insidescript:"&arguments.a;}
			return "script:"&foo();
		}
	</cfscript>
</cfcomponent>

<cfcomponent  name="sub">
	<cffunction name="subTest">
		<cfreturn "subito">
	</cffunction>
	<cffunction name="bb">
		<cfreturn "bb">
	</cffunction>
</cfcomponent>