component {

	this.datasources.test = {
		class: 'org.h2.Driver'
		, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db;MODE=MySQL'
	};
	


   this.name = "bugs-case1";
   this.datasource = "test";
   this.ormEnabled = true;
   this.ormSettings = {
      dbcreate: "dropcreate",
      logSQL=true
   };

}
