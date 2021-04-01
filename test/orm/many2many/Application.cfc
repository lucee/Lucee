<cfcomponent>

<cfscript>
	this.name = hash(getCurrentTemplatePath()) & getTickCount();
 	if(directoryExists("datasource"))directoryDelete("datasource",true);

 	this.datasources.test = {
	  class: "org.h2.Driver"
		, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db;MODE=MySQL'
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