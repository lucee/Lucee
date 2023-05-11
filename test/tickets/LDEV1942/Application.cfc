component {
	this.sessionmanagement = true;
	this.name = createUUID();
	mySQL = getCredentials();
	
	if(mySQL.count()!=0){
		mySQL.storage = true;
		this.datasources["myDataSource"]=mysql;
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}

	this.cache.connections["myCache"] = {
		class: 'lucee.runtime.cache.ram.RamCache'
		, storage: true
		, custom: {
			"timeToIdleSeconds":"5",
			"timeToLiveSeconds":"10"
		}
		, default: ''
	};
	this.NULLSupport=form.setNull;
	this.sessionstorage = form.storage;
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}