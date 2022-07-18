<cfcomponent>
	<cffunction name="callFunction">
		<cfset data = "I'm from 1st function">
		<cfreturn data>
	</cffunction>

	<cffunction name="callFunction">
		<cfset data = "I'm from 2nd function">
		<cfreturn data>
	</cffunction>

	<cffunction name="callFunction">
		<cfset data = "I'm from 3rd function">
		<cfreturn data>
	</cffunction>
</cfcomponent>