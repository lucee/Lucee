<cfcomponent>

<cfscript>
	this.name = hash(getCurrentTemplatePath()) & getTickCount();
 	if(directoryExists("datasource"))directoryDelete("datasource",true);

 	this.datasources.test = {
	  class: 'org.hsqldb.jdbcDriver'
	, connectionString: 'jdbc:hsqldb:file:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db'
	};
	this.datasource = 'test'; 
		
	this.ormEnabled = true; 
	this.ormSettings = { 
		dbcreate = 'dropcreate',
		saveMapping=true,
		cfclocation = 'model'
	} ;
</cfscript>

<cffunction name="onApplicationStart">
	
</cffunction>


<cffunction name="onRequestStart">

</cffunction>

</cfcomponent>