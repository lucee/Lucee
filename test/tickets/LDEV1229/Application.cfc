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
			this.datasource={
				class: 'org.gjt.mm.mysql.Driver'
				, bundleName:'com.mysql.jdbc'
				, bundleVersion:'5.1.38'
				, connectionString: 'jdbc:mysql://'&mySQL.server&':'&mySQL.port&'/'&mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true'
				, username: mySQL.username
				, password: mySQL.password
			};
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
		return server.getDatasource("mysql");
	}
}