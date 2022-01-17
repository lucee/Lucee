<cfcomponent extends="Appender">
	<cfadmin 
		action="getDatasources"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnVariable="datasources">

	<cfset fields=array(
		field("Datasource","datasource",queryColumnData(datasources,"name").toList(),true,
			"Choose a Datasource to log into. Go to Services/Datasource to create a new Datasource. The Storage option must be enabled.","select")
		,field("Table name","table","LOGS",true,"Name of the table log data is logged into.","text")
		,field("Custom","custom","",true,"Custom data passed into every single record created in the datasource.","textarea")
	
		//,field("Charset","charset","UTF-8",true,"charset used to write the file (empty == resource charset)","text")
		//,field("Max Files","maxfiles","10",true,"Maximal amount of Files created, if this number is reached the oldest get destroyed for every new file","text")
		//,field("Max File Size (in bytes)","maxfilesize",10*1024*1024,true,"The maxial size of a log file created in bytes","text")
		
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
		<cfreturn left(getConfigSettings().log4j.version,1)==1?"lucee.commons.io.log.log4j.appender.DatasourceAppender":"lucee.commons.io.log.log4j2.appender.DatasourceAppender">
	</cffunction>
	
	<cffunction name="getLabel" returntype="string" output="false">
		<cfreturn "Datasource">
	</cffunction>
	<cffunction name="getDescription" returntype="string" output="no">
		<cfreturn "Logs to a datasource.">
	</cffunction>
	<cffunction name="getLayout" returntype="string" output="no">
		<cfreturn "lucee.commons.io.log.log4j.layout.DatasourceLayout">
	</cffunction>
	
</cfcomponent>