component {

	this.name	=	'LDEV1793';
	mySQL = getCredentials();

	mySQL = getCredentials();
	if(mySQL.count()!=0){
		this.datasource ="#server.getDatasource("mysql")#";
	}

	// ORM settings
	this.ormEnabled = true;
	this.ormSettings = {
		dialect = "MySQLwithInnoDB"
	};
	function onRequestStart(){

		query {
	        echo("SET FOREIGN_KEY_CHECKS=0");
		}
		query {
	        echo("DROP TABLE IF EXISTS `LDEV1793`");
		}
		query {
	        echo("CREATE TABLE `LDEV1793` (
	          `ID` binary(255),
	          `name` varchar(100)
	        )");
		}
		query {
	        echo("INSERT INTO LDEV1793(id, name) VALUES ('10001100010', 'lucee');");
		}
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}

}