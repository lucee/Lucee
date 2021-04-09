component {
	this.name =	"sample";
	mySQL = getCredentials();
	this.datasource =  = mySQL;
	
	function onRequestStart(){
		setting showdebugOutput=false;
	}

	function onApplicationStart(){
		query{
			echo("DROP TABLE IF EXISTS pages");
		}
		query{
			echo("CREATE TABLE pages (page_id INT(11) NOT NULL AUTO_INCREMENT, page_title VARCHAR(150) DEFAULT NULL, PRIMARY KEY (page_id)) ENGINE=INNODB DEFAULT CHARSET=utf8");
		}
		query{
			echo("INSERT INTO pages (page_id, page_title) VALUES (1,'Page 1'),(2,'Page 2')");
		}
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}
