component {

	this.name = "orm-events";
	this.datasource= server.getDatasource("h2", server._getUniqueTempDir("orm-events") );
	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate: "dropcreate",
		//dbCreate: "none",
		eventHandling: true,
		eventHandler: "eventHandler",
		autoManageSession: false,
		flushAtRequestEnd: false,
		useDBForMapping: false,
		dialect: "h2"
	};

	function onApplicationStart() {
		application.ormEventLog = [];
	}

	public function onRequestStart() {
		setting requesttimeout=10;
		application.ormEventLog = [];
		if ( url.keyExists( "flushcache" ) ){
			componentCacheClear();
		}
	}

}