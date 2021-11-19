component {

	this.name	=	'test';
	this.sessionManagement 	= false;
	
	if(url.db=='h2') {
		this.datasource={
	  		class: 'org.h2.Driver'
	  		, bundleName:"org.h2"
			, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db;MODE=MySQL'
		};

	}
	else {
		mySQL = getCredentials();
		if(mySQL.count()!=0){
			this.datasource=server.getDatasource("mysql");
		}
	}

	// ORM settings
	this.ormEnabled = true;
	this.ormSettings = {
		autoManageSession = false
		,flushAtRequestEnd = false
		,dialect = "MySQLwithInnoDB"
	};
	function onRequestStart(){
		if(url.db=='mysql') {
			query {
		        echo("SET FOREIGN_KEY_CHECKS=0");
			}
		}
		query {
	        echo("DROP TABLE IF EXISTS `child`");
		}
		query {
	        echo("DROP TABLE IF EXISTS `parent`");
		}
		query {
	        echo("CREATE TABLE `child` (
	          `ID` int(10) unsigned NOT NULL,
	          `parentID` int(10) unsigned DEFAULT NULL,
	          PRIMARY KEY (`ID`)
	        );");
		}
		query {
	        echo("CREATE TABLE `parent` (
				  `ID` int(10) unsigned NOT NULL,
				  PRIMARY KEY (`ID`)
				);");
		}
		query {
	        echo("INSERT INTO `parent` VALUES ('1');");
		}
		if(url.db=='mysql') {
			query {
	        	echo("SET FOREIGN_KEY_CHECKS=1");
			}
		}
	}

	private struct function getCredentials() {
		// getting the credentials from the enviroment variables
		var mySQL={};
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

	function onRequestEnd() {
		var javaIoFile=createObject("java","java.io.File");
		loop array=DirectoryList(
			path=getDirectoryFromPath(getCurrentTemplatePath()), 
			recurse=true, filter="*.db") item="local.path"  {
			fileDeleteOnExit(javaIoFile,path);
		}
	}

	private function fileDeleteOnExit(required javaIoFile, required string path) {
		var file=javaIoFile.init(arguments.path);
		if(!file.isFile())file=javaIoFile.init(expandPath(arguments.path));
		if(file.isFile()) file.deleteOnExit();
	}
}