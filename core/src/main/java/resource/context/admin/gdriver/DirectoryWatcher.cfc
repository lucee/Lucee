<cfcomponent extends="Gateway">

	
    <cfset fields=array(
		field("Directory","directory","",true,"The directory you want to watch","text")
		,field("Watch subdirectories","recurse","true",true,"Should we watch the directory and all subdirectories too","checkbox")
		,field("Interval (ms)","interval","60000",true,"The interval between checks, in miliseconds","text")
		,field("File filter","extensions","*",true,"The comma separated list of file filters to match (* = all files). Examples: *user*,*.gif,2010*,myfilename.txt","text")

		,group("CFC Listener Function Defintion","Definition for the CFC Listener Functions, when empty no listener is called",3)

		,field("Change","changeFunction","onChange",true,"called when a file change","text")
		,field("Add","addFunction","onAdd",true,"called when a file is added","text")
		,field("Delete","deleteFunction","onDelete",true,"called when a file is removed","text")
	)>

	<cffunction name="getClass" returntype="string">
    	<cfreturn "">
    </cffunction>
	<cffunction name="getCFCPath" returntype="string">
    	<cfreturn "lucee.extension.gateway.DirectoryWatcher">
    </cffunction>
    
	<cffunction name="getLabel" returntype="string" output="no">
    	<cfreturn "Directory Watcher">
    </cffunction>
	<cffunction name="getDescription" returntype="string" output="no">
    	<cfreturn "Watch a certain directory for changes">
    </cffunction>
    
	<cffunction name="onBeforeUpdate" returntype="void" output="false">
		<cfargument name="cfcPath" required="true" type="string">
		<cfargument name="startupMode" required="true" type="string">
		<cfargument name="custom" required="true" type="struct">
        
        <!--- directory --->
        <cfif not DirectoryExists(custom.directory)>
        	<cfthrow message="directory [#custom.directory#] does not exist">
        </cfif>
        
        <!--- interval --->
        <cfif not IsNumeric(custom.interval)>
        	<cfthrow message="interval [#custom.interval#] is not a numeric value">
        <cfelseif custom.interval LT 1>
        	<cfthrow message="interval [#custom.interval#] must be a positive number greater than 0">
        </cfif>
        
        
        
	</cffunction>
    
    
	<cffunction name="getListenerCfcMode" returntype="string" output="no">
		<cfreturn "required">
	</cffunction>
	<cffunction name="getListenerPath" returntype="string" output="no">
		<cfreturn "lucee.extension.gateway.DirectoryWatcherListener">
	</cffunction>
</cfcomponent>

