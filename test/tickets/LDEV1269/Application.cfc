component {

	this.name	=	Hash( GetCurrentTemplatePath() );
	this.sessionManagement 	= false;

	mySQL=getCredentials();

	this.datasources["sample"] = mySQL;
	this.datasource = "sample";


	public function onRequestStart() {
		setting requesttimeout=10 showdebugOutput=false;
		// init the table used
		query {
	        echo("SET FOREIGN_KEY_CHECKS=0");
		}
		query {
	        echo("DROP TABLE IF EXISTS `test`");
		}
		query {
	        echo("CREATE TABLE `test` (
	`id` int(11) unsigned NOT NULL
	)");
		}
		query {
	        echo("INSERT INTO `test` VALUES ('4294967295'), ('4294967294'), ('4294967293');");
		}
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}

}