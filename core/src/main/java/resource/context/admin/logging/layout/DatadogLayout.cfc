<cfcomponent extends="Layout">
	
    <cfset fields=array(
		)>
    
	<cffunction name="getClass" returntype="string">
    	<cfreturn "lucee.commons.io.log.log4j2.layout.DataDogLayout">
    </cffunction>
    
	<cffunction name="getLabel" returntype="string" output="false">
    	<cfreturn "Datadog">
    </cffunction>
	<cffunction name="getDescription" returntype="string" output="no">
    	<cfreturn "Layout for logging to Datadog via the Datadog javaAgent">
    </cffunction>
    
</cfcomponent>