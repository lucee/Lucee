<cfcomponent output="no">
	<cfset this.data=struct()>
	<cffunction name="init"
		hint="init method of the component">
		<cfargument name="displayName" required="true" type="string">
		<cfargument name="name" required="true" type="string">
		<cfargument name="defaultValue" required="false" type="string" default="">
		<cfargument name="required" required="false" type="boolean" default="no">
		<cfargument name="description" required="false" type="any" default="">
		<cfargument name="type" required="false" type="string" default="text">
		<cfargument name="values" required="false" type="string" default="">
		
		<cfset this.data.displayName=arguments.displayName>
		<cfset this.data.name=trim(arguments.name)>
		<cfset this.data.required=arguments.required>
		<cfset this.data.defaultValue=arguments.defaultValue>
		<cfset this.data.values=arguments.values>
		<cfset this.data.description=arguments.description>
		<cfset this.data.type=arguments.type>
		
		<cfreturn this>
	</cffunction>
	
	<cffunction name="getDisplayName" returntype="string" output="no"
		hint="returns the Display Name">
		<cfreturn this.data.displayName>
	</cffunction>
	
	<cffunction name="getName" returntype="string" output="no"
		hint="returns the Name">
        <cfreturn this.data.name>
	</cffunction>
	
	<cffunction name="getDefaultValue" returntype="string" output="no"
		hint="returns the Display Name">
		<cfreturn this.data.defaultValue>
	</cffunction>
	<cffunction name="getValues" returntype="string" output="no"
		hint="returns the Display Name">
        <cfif len(this.data.values)>
        	<cfreturn this.data.values>
        </cfif>
		<cfreturn this.data.defaultValue>
	</cffunction>
	
	<cffunction name="getRequired" returntype="boolean" output="no"
		hint="returns the required value">
		<cfreturn this.data.required>
	</cffunction>
    
	<cffunction name="getData" returntype="struct" output="no"
		hint="returns all data as struct">
		<cfreturn this.data>
	</cffunction>
	
	<cffunction name="getDescription" returntype="any" output="no"
		hint="returns the description value">
		<cfreturn this.data.description>
	</cffunction>
	
	<cffunction name="getType" returntype="string" output="no"
		hint="returns the type of the field, types are [text,password,radio,chckbox,select]">
		<cfreturn this.data.type>
	</cffunction>
</cfcomponent>