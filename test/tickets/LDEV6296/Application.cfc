component {

	this.name = "LDEV6296_miniapp_" & createUniqueId();
	this.applicationTimeout = createTimeSpan( 0, 0, 5, 0 );

	this.datasources[ "LDEV6296_app" ] = {
		  class            : "org.hsqldb.jdbcDriver"
		, bundleName       : "org.lucee.hsqldb"
		, connectionString : "jdbc:hsqldb:mem:LDEV6296_app;shutdown=true"
		, username         : "sa"
		, password         : ""
	};

}
