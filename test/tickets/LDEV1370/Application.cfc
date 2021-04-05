component {
	this.name 				= "LDEV1370" & hash( getCurrentTemplatePath() );

	this.datasource={
	  		class: 'org.h2.Driver'
	  		, bundleName: 'org.h2'
			, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db1370;MODE=MySQL;MVCC=true'
		};

	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate = "update",
		secondarycacheenabled = false,
		//flushAtRequestEnd 	= true,
		//autoManageSession	= true,
		secondaryCacheEnabled = false,
		eventhandling = true
	};

	if(!isNull(url.flushAtRequestEnd)) this.ormSettings.flushAtRequestEnd=url.flushAtRequestEnd;
	if(!isNull(url.autoManageSession)) this.ormSettings.autoManageSession=url.autoManageSession;
}