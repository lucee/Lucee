<cfcomponent extends="Appender">
	
    <cfset fields=array(
		field("Path","path","{lucee-config}/logs/",true,"Path to the file (any virtual filesystem supported)","text")
		,field("Charset","charset","UTF-8",true,"charset used to write the file (empty == resource charset)","text")
		,field("Max Files","maxfiles","10",true,"Maximal amount of Files created, if this number is reached the oldest get destroyed for every new file","text")
		,field("Max File Size (in bytes)","maxfilesize",10*1024*1024,true,"The maxial size of a log file created in bytes","text")
		,field("Stream Timeout (in minutes)","timeout","1",true,
			"Defines a timeout in minutes of inactivity after which the stream to the log resource will be automatically closed. If set to [0] a new stream is used for every request to the log resource (slow).","select","0,1,2,3,4,5,6")
		
		)>
		
	<cffunction name="getCustomFields" returntype="array" output="false">
		<cfif !isNull(form._name)>
			<cfset var fields=duplicate(variables.fields)>
			<cfloop array="#fields#" index="local.i" item="local.field">
				<cfif field.getName() EQ "Path">
					<cfset local.dv=field.getDefaultValue()>
					<cfif right(dv,1) NEQ "/" and right(dv,1) NEQ "\">
						<cfset dv&="/">
					</cfif>
					<cfset field.setDefaultValue(dv&form._name&".log")>
				</cfif>
			
			</cfloop>
			
			
			
		</cfif>
		<cfreturn fields>
    </cffunction>
    
	<cffunction name="getClass" returntype="string" output="false">
    	<cfreturn "resource">
    </cffunction>
    
	<cffunction name="getLabel" returntype="string" output="false">
    	<cfreturn "Resource">
    </cffunction>
	<cffunction name="getDescription" returntype="string" output="no">
    	<cfreturn "Logs to a resource (locale file, ftp, zip, ...)">
    </cffunction>
    
</cfcomponent>