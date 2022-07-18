component {
	this.name = "test3";
	mySQL= getCredentials();
	this.datasource = {
		 type: "mysql"
		 ,host: "#mySQL.server#"
		 ,port: "#mySQL.port#"
		 ,database: "#mySQL.database#?"
		 ,username: "#mySQL.username#"
		 ,password: "#mySQL.password#"
		 ,custom: { useUnicode:true }
	};

	public function onRequestStart() {
		setting requesttimeout=10;
	}
	
	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}

	private function createTable() {
		query {
	        echo("DROP TABLE IF EXISTS users1323;");
		}
		query {
	        echo("CREATE TABLE users1323 (sNo varchar(50), FirstName varchar(50), Title varchar(50))");
		}
		query {
	        echo("INSERT INTO users1323 (sNo,FirstName,Title) VALUES (22,'john','test'),(33,'jose','sample');");
		}
	}
}