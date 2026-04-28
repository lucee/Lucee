component {

	this.name = "LDEV6293_miniapp_" & createUniqueId();
	this.applicationTimeout = createTimeSpan( 0, 0, 5, 0 );

	this.datasources[ "LDEV6293_app" ] = {
		  class            : "org.hsqldb.jdbcDriver"
		, connectionString : "jdbc:hsqldb:mem:LDEV6293_app;shutdown=true"
		, username         : "sa"
		, password         : ""
	};

}
