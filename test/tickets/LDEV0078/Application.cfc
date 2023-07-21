component {

	this.name	=	Hash( GetCurrentTemplatePath() );
	this.sessionManagement 	= false;	

 	
	mySQL=getCredencials();

	this.datasource ={
		  class: 'org.gjt.mm.mysql.Driver'
		, bundleName:'com.mysql.jdbc'
		, bundleVersion:'5.1.38'
		, connectionString: 'jdbc:mysql://'&mySQL.server&':'&mySQL.port&'/'&mySQL.database&'?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true'
		, username: mySQL.username
		, password: mySQL.password
	};
	
	

	// ORM settings
	this.ormEnabled = true;
	this.ormSettings = {
		//dialect = "MySQLwithInnoDB",
		autoManageSession = false,
		flushAtRequestEnd = false
	};

	function onRequestStart(){
		setting showdebugOutput=false;
		// init the table used
		query {
	        echo("SET FOREIGN_KEY_CHECKS=0");
		}
		query {
	        echo("DROP TABLE IF EXISTS `test`");
		}
		query {
	        echo("CREATE TABLE `test` (
	`id` int(11) unsigned NOT NULL AUTO_INCREMENT,
	`name` varchar(50) DEFAULT NULL,
	PRIMARY KEY (`id`)
	)");
		}
		query {
	        echo("INSERT INTO `test` VALUES ('1', null);");
		}
	}


	private struct function getCredencials() {
		return server.getDatasource("mysql");
	}
	


}