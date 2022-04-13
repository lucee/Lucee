component {
	this.name = "orm" & hash( getCurrentTemplatePath() );

    // H2 config - gives me a lock timeout error
	// this.datasource={
    //     class: 'org.h2.Driver'
    //     , bundleName: 'org.h2'
    //     , connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db;MODE=MySQL'
    // };
    // this.ormSettings = {
	// 	dbcreate = "dropcreate"
	// };
    // mssql setup
    this.datasource = server.getDatasource("mssql");

	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate = "dropcreate",
		dialect  = "org.hibernate.dialect.SQLServer2008Dialect"
	};

}