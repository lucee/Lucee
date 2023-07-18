<cfcomponent>

<cfscript>
	this.name = hash(getCurrentTemplatePath()) & getTickCount();
 	this.datasources.test = server.getDatasource("h2", server._getTempDir("orm-many2many") );
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