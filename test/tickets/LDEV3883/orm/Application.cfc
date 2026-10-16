component {

	if (isNull(url.type)) url.type = "";
	if (isNull(url.appName)) url.appName = "";

	this.name = "#createUUID()#";
	this.sessionManagement = true;
	this.sessionTimeout =createTimespan(0,0,0,10);
	this.datasource = {
		class: "org.h2.Driver"
		, connectionString: 'jdbc:h2:#server._getTempDir( url.appName )#/db;MODE=MySQL'
	};
	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate: "dropCreate",
		eventHandling: true,
		eventHandler: "eventHandler",
		dialect: "H2"
	};

	function onApplicationStart() {
		writeoutput("onApplicationStart,");
	}

	function onSessionStart() {
		writeoutput("onSessionStart,");
	}

	function onRequestStart() {
		writeoutput("onRequestStart,");
	}

	function onRequestEnd() {
		writeoutput("onRequestEnd");
	}

} 