<cfcomponent>
	
    <cfset fields=array()>

	<cffunction name="field" returntype="component" access="private" output="no">
		<cfargument name="displayName" required="true" type="string">
		<cfargument name="name" required="true" type="string">
		<cfargument name="defaultValue" required="false" type="string" default="">
		<cfargument name="required" required="false" type="boolean" default="no">
		<cfargument name="description" required="false" type="any" default="">
		<cfargument name="type" required="false" type="string" default="text">
		<cfargument name="values" required="false" type="string" default="">
		<cfreturn createObject("component","Field").init(	arguments.displayName,
															arguments.name,
															arguments.defaultValue,
															arguments.required,
															arguments.description,
															arguments.type,
															arguments.values)>
	</cffunction>
	<cffunction name="group" returntype="component" access="private" output="no">
		<cfargument name="displayName" required="true" type="string">
		<cfargument name="description" required="false" type="string" default="">
		<cfargument name="level" required="false" type="numeric" default="2">
		<cfreturn createObject("component","Group").init(arguments.displayName,arguments.description,arguments.level)>
	</cffunction>


	<cffunction name="getCustomFields" returntype="array">
    	<cfreturn fields>
    </cffunction>
    
    
	<cffunction name="onBeforeUpdate" returntype="void" output="false">
		<cfargument name="custom" required="true" type="struct">
        
	</cffunction>
	
	<cffunction name="onBeforeError" returntype="void" output="no">
		<cfargument name="cfcatch" required="true" type="struct">
	</cffunction>
	
    
    
	<cffunction name="getId" returntype="string">
    	<cfthrow message="implement function getID():string">
    </cffunction>
    
	<cffunction name="output" returntype="string">
		<cfargument name="custom" required="true" type="struct">
		<cfargument name="debugging" required="true" type="struct">
    	<cfthrow message="implement function output():string">
    </cffunction>
    
    
    
</cfcomponent>