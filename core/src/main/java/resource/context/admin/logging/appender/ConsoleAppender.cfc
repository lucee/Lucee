<cfcomponent extends="Appender">
	
    <cfset fields=array(
		field("Stream type","streamtype","output,error",true,"define if the appender logs to the error or output stream","select")
		
		)>
    
	<cffunction name="getClass" returntype="string" output="false">
    	<cfreturn "lucee.commons.io.log.log4j2.appender.ConsoleAppender">
    </cffunction>
    
	<cffunction name="getLabel" returntype="string" output="false">
    	<cfreturn "Console">
    </cffunction>
	<cffunction name="getDescription" returntype="string" output="no">
    	<cfreturn "Logs events to to the error or output stream">
    </cffunction>
    
</cfcomponent>