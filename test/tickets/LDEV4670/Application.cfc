component {
	param name="form.timezone";
	param name="form.sessionEnable" default="true";
	param name="form.sessionStorageType";

	this.name =	"ldev-4670-mysql-#form.timezone#";

	this.sessionManagement = form.sessionEnable;
	this.sessionTimeout = createTimeSpan(0,0,0,1);	

	switch ( form.sessionStorageType ){
		case "memory":
			this.sessionStorage = "memory";
			break;
		case "datasource":
			mySQL = mySqlCredentials();
			mySQL.storage = true;
			datasource = "my-ldev-4670"
			this.datasources[datasource] = mySQL;
			this.dataSource = datasource;
			this.sessionStorage = datasource;
			break;
		default:
			throw "unsupported sessionStorageType [#form.sessionStorageType#]";
	}
	
	this.timezone = form.timezone; // Pacific Time Zone, UTC -7
	
	public function onRequestStart() {
		// systemOutput(getTimeZone(), true);
		setting requesttimeout=10 showdebugOutput=false;
		if ( form.sessionEnable ) {
			session["running"] = true;
			session["sessionStorageType"] = form.sessionStorageType;
			param name="session.requestCount" default="0";
			session["requestCount"]++;
			session["timezone"] = form.timezone;
		}
	}

	function onSessionEnd( SessionScope, ApplicationScope ) {
		systemOutput("!!!!!!!!!!!!!!!!!!!!!!!! #now()# session ended #cgi.SCRIPT_NAME# #sessionScope.sessionid#", true);
		server.LDEV4670_endedSessions[ arguments.sessionScope.sessionid ] = now();
	}

	private struct function mySqlCredentials() {
		return server.getDatasource("mysql");
	}
}