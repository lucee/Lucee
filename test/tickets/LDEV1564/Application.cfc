component {
	this.name =	"test123";
	this.ApplicationTimeout = CreateTimeSpan( 2, 0, 0, 0 );
	this.clientManagement = true;
	this.sessionmanagement = true;
	this.SessionTimeout = CreateTimeSpan( 0, 0, 10, 0 );
	this.setclientcookies = True ;

	this.ormenabled = true;
	this.ormsettings.cfclocation = ["/go"];
	this.ormsettings.dbCreate = "none";
	this.ormsettings.autoManageSession=false;
	this.ormsettings.dialect = "MicrosoftSQLServer";

	msSQL = server.getDatasource("mssql");
	msSQL.storage = true;
	datasource = "ormTest";
	this.datasources[datasource] = msSQL;

	this.dataSource = datasource;

	function onApplicationStart(){
		query{
			echo("IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'users') BEGIN DROP TABLE users END");
   		}
		query{
			echo("CREATE TABLE users( uid INT IDENTITY(1,1) PRIMARY KEY, uName varchar(50) )");
		}
	}
}