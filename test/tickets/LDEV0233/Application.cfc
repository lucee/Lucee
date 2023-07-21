component output="false" {

	this.name = 'ORMTransaction';
	this.sessionmanagement = true;

	mySQL = getCredentials();

	this.ormenabled = true;
	this.datasources["trans"] = {
		class: 'org.gjt.mm.mysql.Driver'
		, bundleName:'com.mysql.jdbc'
		, bundleVersion:'5.1.38'
		, connectionString: 'jdbc:mysql://'&mySQL.server&':'&mySQL.port&'/'&mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true'
		, username: mySQL.username
		, password: mySQL.password
	};
	this.ormsettings={datasource="trans"
		,logsql="false"
		,cfclocation="model"
		,dbcreate="dropcreate"
		,dialect="MySQL"
		,flushatrequestend=false
	};

	public function onRequestStart() {
		oRMReload();
		saveData = EntityNew('ContentType');
		saveData.setid(1);
		saveData.setTitle("Title");
		EntitySave(saveData);
		oRMFlush();
	}

	// Private function to get dsn details from env vars
	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}

}
