component {

	this.name	=	Hash( GetCurrentTemplatePath() );
	this.sessionManagement 	= false;

	mySQL=getCredencials();

	this.datasources["sample"] = mySQL;
	this.datasource = "sample";

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
	`id` int(11) unsigned NOT NULL
	)");
		}
		query {
	        echo("INSERT INTO `test` VALUES ('4294967295'), ('4294967294'), ('4294967293');");
		}
	}

	private struct function getCredencials() {
		return server.getDatasource("mysql");
	}

}