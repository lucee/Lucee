
component {
	this.name 	= "LDEV1428" & hash( getCurrentTemplatePath() );
	this.datasource={
  		class: 'org.h2.Driver'
  		, bundleName: 'org.h2'
		, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db1428;MODE=MySQL'
	};

	this.ormEnabled = true;
	this.ormSettings = {
		savemapping=true,
		dbcreate = "update",
		secondarycacheenabled = false,
		logSQL 				= true,
		flushAtRequestEnd 	= false,
		autoManageSession	= false,
		skipCFCWithError	= false
	};

}
