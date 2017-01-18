// <cflucee>
<cfcomponent output="false" >
	
	<cffunction  name="subFunction" >
		container(one, two);
	</cffunction>

	<cffunction name="container" >
		<cfoutput>#arguments.one#</cfoutput>
	</cffunction>
</cfcomponent>

