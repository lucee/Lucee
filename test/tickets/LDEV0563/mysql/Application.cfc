component {
	this.name = hash( getCurrentTemplatePath() );
	request.baseURL = "http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath = GetDirectoryFromPath(getCurrentTemplatePath());

	mySQL = getCredentials();

	this.datasources["DSN1"] = {
		  class: 'org.gjt.mm.mysql.Driver'
		, bundleName:'com.mysql.jdbc'
		, bundleVersion:'5.1.38'
		, connectionString: 'jdbc:mysql://'&mySQL.server&':'&mySQL.port&'/'&mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true'
		, username: mySQL.username
		, password: mySQL.password
	};

	this.datasources["DSN2"] = {
		  class: 'org.gjt.mm.mysql.Driver'
		, bundleName:'com.mysql.jdbc'
		, bundleVersion:'5.1.38'
		, connectionString: 'jdbc:mysql://'&mySQL.server&':'&mySQL.port&'/'&mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true'
		, username: mySQL.username
		, password: mySQL.password
	};

	this.datasources["DSN3"] = {
		class: 'org.gjt.mm.mysql.Driver'
		, bundleName:'com.mysql.jdbc'
		, bundleVersion:'5.1.38'
		, connectionString: 'jdbc:mysql://'&mySQL.server&':'&mySQL.port&'/'&mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true'
		, username: mySQL.username
		, password: mySQL.password
	};

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
		// getting the credentials from the environment variables
		var mySQL={};
		if(isNull(server.system)){
			server.system = structNew();
			currSystem = createObject("java", "java.lang.System");
			server.system.environment = currSystem.getenv();
			server.system.properties = currSystem.getproperties();
		}

		if(
			!isNull(server.system.environment.MYSQL_SERVER) &&
			!isNull(server.system.environment.MYSQL_USERNAME) &&
			!isNull(server.system.environment.MYSQL_PASSWORD) &&
			!isNull(server.system.environment.MYSQL_PORT) &&
			!isNull(server.system.environment.MYSQL_DATABASE)) {
			mySQL.server=server.system.environment.MYSQL_SERVER;
			mySQL.username=server.system.environment.MYSQL_USERNAME;
			mySQL.password=server.system.environment.MYSQL_PASSWORD;
			mySQL.port=server.system.environment.MYSQL_PORT;
			mySQL.database=server.system.environment.MYSQL_DATABASE;
		}
		// getting the credentials from the system variables
		else if(
			!isNull(server.system.properties.MYSQL_SERVER) &&
			!isNull(server.system.properties.MYSQL_USERNAME) &&
			!isNull(server.system.properties.MYSQL_PASSWORD) &&
			!isNull(server.system.properties.MYSQL_PORT) &&
			!isNull(server.system.properties.MYSQL_DATABASE)) {
			mySQL.server=server.system.properties.MYSQL_SERVER;
			mySQL.username=server.system.properties.MYSQL_USERNAME;
			mySQL.password=server.system.properties.MYSQL_PASSWORD;
			mySQL.port=server.system.properties.MYSQL_PORT;
			mySQL.database=server.system.properties.MYSQL_DATABASE;
		}
		return mysql;
	}
}