component {

	this.name	=	Hash( GetCurrentTemplatePath() );
	this.sessionManagement 	= false;	

	this.datasource =server.getDatasource("mysql");

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
}