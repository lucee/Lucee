<cfcomponent extends="Appender">
	
    <cfset fields=array(
		field("Datasource Name","datasource","",true,"name of the datasource to use for the appender","text")
		,field("Username","username","",true,"name of the datasource to use for the appender","text")
		,field("Password","password","",true,"name of the datasource to use for the appender","text")
		
		)>
    
	<cffunction name="getClass" returntype="string" output="false">
    	<cfreturn "lucee.commons.io.log.log4j.appender.DatasourceAppender">
    </cffunction>
    
	<cffunction name="getLabel" returntype="string" output="false">
    	<cfreturn "Datasource">
    </cffunction>
	<cffunction name="getDescription" returntype="string" output="no">
    	<cfreturn "Logs events to a datasource">
    </cffunction>
    
</cfcomponent>