component {
	this.name =	"App1";
	this.clientManagement = true;
	this.sessionmanagement = true;
	this.sessionTimeout = createTimeSpan(0,0,30,0);

	mySQL = mySqlCredentials();
	mySQL.storage = true;
	datasource = "my-ldev-215"
	this.datasources[datasource] = mySQL;
	this.dataSource = datasource;

	this.clientStorage = datasource;
	this.sessionStorage = datasource;

	
	public function onRequestStart() {
		setting requesttimeout=10 showdebugOutput=false;
	}

	function onApplicationStart(){
		query {
			echo("DROP TABLE IF EXISTS cf_session_data");
		}
		query {
			echo("DROP TABLE IF EXISTS cf_client_data");
		}
		session.test = "App1";
		client.test = "App2";
	}

	private struct function mySqlCredentials() {
		return server.getDatasource("mysql");
	}
}