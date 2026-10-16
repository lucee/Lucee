component {
	this.name = "ldev-6046-mysql-" & hash( getCurrentTemplatePath() );
	this.sessionManagement = true;
	this.sessionTimeout = createTimeSpan( 0, 0, 5, 0 );
	this.setClientCookies = true;

	mySQL = mySqlCredentials();
	mySQL.storage = true;
	datasource = "ldev-6046-ds";
	this.datasources[ datasource ] = mySQL;
	this.dataSource = datasource;

	// Use database for session storage - this is what the bug report is about
	this.sessionStorage = datasource;
	// Bug reporter had sessionCluster=true, but we also test sessionCluster=false
	this.sessionCluster = url.sessionCluster ?: true;

	public function onRequestStart() {
		setting requesttimeout=10 showdebugOutput=false;
	}

	function onApplicationStart() {
		// Cleanup any existing session table
		try {
			query {
				echo( "DROP TABLE IF EXISTS cf_session_data" );
			}
		}
		catch ( any e ) {
			// ignore
		}
	}

	function onSessionStart() {
		systemOutput("------onSessionStart", true);
		session.started = now();
	}

	function onSessionEnd( sessionScope, applicationScope ) {
		systemOutput( "------onSessionEnd", true );
	}

	private struct function mySqlCredentials() {
		return server.getDatasource( "mysql" );
	}
}
