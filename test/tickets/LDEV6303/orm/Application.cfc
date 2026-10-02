component {
	this.name = "LDEV6303_orm_test";
	this.ORMenabled = "true";
	this.ORMsettings = {
		datasource = "LDEV6303",
		dbCreate = "dropCreate",
		dialect = "MySQL"
	};
	this.datasource = "LDEV6303";

	// hardwired h2 — works under both HTTP runner and mvn test runner
	tempDb = getTempDirectory() & "/LDEV6303-" & hash( getCurrentTemplatePath() );
	if ( !directoryExists( tempDb ) ) directoryCreate( tempDb, true );

	this.datasources[ "LDEV6303" ] = {
		class: "org.h2.Driver",
		bundleName: "org.lucee.h2",
		bundleVersion: "2.1.214.0001L",
		connectionString: "jdbc:h2:" & tempDb & "/db;MODE=MySQL"
	};
}
