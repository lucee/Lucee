component {
	this.name =	"App2";
	this.clientManagement = true;
	this.sessionmanagement = true;
	this.sessionTimeout = createTimeSpan(0,0,30,0);

	msSQL = server.getDatasource("mssql");
	msSQL.storage = true;

	datasource = "ms-ldev-215";	
	this.datasources[datasource] = msSQL

	this.dataSource = datasource;
	this.clientStorage = datasource;
	this.sessionStorage = datasource;

	function onRequestStart(){
		setting showdebugOutput=false;
	}

	function onApplicationStart(){
		query {
			echo("IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'cf_client_data') BEGIN DROP TABLE cf_client_data END");
		}
		query {
  			echo("IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'cf_session_data') BEGIN DROP TABLE cf_session_data END");
		}
		session.test = "App2";
		client.test = "App2";
	}

}