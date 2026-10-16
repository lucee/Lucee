component {
	this.name = "ldev-5930-" & hash( getCurrentTemplatePath() );
	this.sessionManagement = true;
	this.sessionTimeout = createTimeSpan( 0, 0, 5, 0 );
	this.setClientCookies = true;

	mySQL = server.getDatasource( "mysql" );
	mySQL.storage = true;
	datasource = "ldev-5930-ds";
	this.datasources[ datasource ] = mySQL;
	this.dataSource = datasource;

	this.sessionStorage = datasource;
	this.sessionCluster = url.sessionCluster ?: false;

	public function onRequestStart() {
		setting requesttimeout=10 showdebugOutput=false;
	}

	function onApplicationStart() {
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
		session.user = new SessionComponent();
	}
}
