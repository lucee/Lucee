component {
	param name="form.timezone";
	param name="form.sessionEnable" default="true";

	this.name =	"ldev-4670-mysql-#form.timezone#";
	
	
	this.sessionManagement = form.sessionEnable;
	this.sessionTimeout = createTimeSpan(0,0,0,1);

	mySQL = mySqlCredentials();
	mySQL.storage = true;
	datasource = "my-ldev-4670"
	this.datasources[datasource] = mySQL;
	this.dataSource = datasource;

	this.sessionStorage = datasource;

	this.timezone = form.timezone; // Pacific Time Zone, UTC -7
	
	public function onRequestStart() {
		// systemOutput(getTimeZone(), true);
		setting requesttimeout=10 showdebugOutput=false;
		if ( form.sessionEnable ) {
			session.running = true;
		}
	}

	private struct function mySqlCredentials() {
		return server.getDatasource("mysql");
	}
}