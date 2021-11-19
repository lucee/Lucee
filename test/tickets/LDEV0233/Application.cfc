component output="false" {

	this.name = 'ORMTransaction';
	this.sessionmanagement = true;

	mySQL = getCredentials();

	this.ormenabled = true;
	this.datasources["trans"] = server.getDatasource("mysql");
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
