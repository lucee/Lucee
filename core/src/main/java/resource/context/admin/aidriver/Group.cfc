<cfcomponent output="no">
	<cfset this.data=struct()>
	<cffunction name="init"
		hint="init method of the group">
		<cfargument name="displayName" required="true" type="string">
		<cfargument name="description" required="false" type="string" default="">
		<cfargument name="level" required="false" type="numeric" default="2">
		
		<cfset this.data.displayName=arguments.displayName>
		<cfset this.data.description=arguments.description>
		<cfset this.data.level=arguments.level>
		
		<cfreturn this>
	</cffunction>
	
	<cffunction name="getDisplayName" returntype="string" output="no"
		hint="returns the Display Name">
		<cfreturn this.data.displayName>
	</cffunction>
	
	<cffunction name="getDescription" returntype="string" output="no"
		hint="returns the description value">
		<cfreturn this.data.description>
	</cffunction>
	<cffunction name="getLevel" returntype="numeric" output="no"
		hint="returns the level">
		<cfreturn this.data.level>
	</cffunction>
	
</cfcomponent>