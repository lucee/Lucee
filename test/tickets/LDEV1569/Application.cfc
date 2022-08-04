component {
	this.name 	= "LDEV-1569";

	mySQL=getCredentials();

	if(mySQL.count()!=0){
		this.datasource=mySql;
	}

	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate = "update",
		secondaryCacheEnabled = false,
		eventhandling = true
	};

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
					`id` int(11) ,
					`name` varchar(50)
					)"
	       		 );
		}
		query {
	        echo("INSERT INTO `test` VALUES ('1', null);");
		}
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}