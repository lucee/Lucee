component {

	this.datasources.test = {
	  class: 'org.hsqldb.jdbcDriver'
	, connectionString: 'jdbc:hsqldb:file:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db'
	};
	


   this.name = "bugs-case1";
   this.datasource = "test";
   this.ormEnabled = true;
   this.ormSettings = {
      dbcreate: "dropcreate",
      logSQL=true
   };

}
