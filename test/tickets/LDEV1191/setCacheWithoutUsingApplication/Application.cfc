component {
	this.name =	Hash( GetCurrentTemplatePath() ) & "3s";
	mySQL = getCredentials();
	this.datasource = mySQL;

	function onRequestStart(){
		setting showdebugOutput=false;
	}

	function onApplicationStart(){
		query{
			echo("DROP TABLE IF EXISTS usersDetails");
		}
		query{
			echo("CREATE TABLE usersDetails( id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(30) )");
		}
		query{
			echo("INSERT INTO usersDetails VALUES(1,'testUser')");
		}
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}
