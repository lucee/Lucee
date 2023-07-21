component {
	this.name = hash( getCurrentTemplatePath() );
	request.baseURL = "http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath = GetDirectoryFromPath(getCurrentTemplatePath());

	mySQL = getCredentials();

	this.datasources["DSN1"] = mySQL;

	this.datasources["DSN2"] = mysql;

	this.datasources["DSN3"] = mysql;

	this.datasource = "DSN1";

	function onRequestStart(){
		setting showdebugOutput=false;
		query{
			echo("DROP TABLE IF EXISTS users1;");
		}
		query{
			echo("DROP TABLE IF EXISTS users2;");
		}
		query{
			echo("DROP TABLE IF EXISTS users3;");
		}
		query{
			echo("CREATE TABLE users1( Name varchar(50) )");
		}
		query{
			echo("CREATE TABLE users2( Name varchar(50) )");
		}
		query{
			echo("CREATE TABLE users3( Name varchar(50) )");
		}
	}

	// Private function to get dsn details from env vars
	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}