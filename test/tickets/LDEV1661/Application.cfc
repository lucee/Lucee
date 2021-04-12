component {
	this.name =	"tests345";
	mySQL = getCredentials();
	this.datasource = mysql;

	function onRequestStart(){
		setting showdebugOutput=false;
	}

	function onApplicationStart(){
		query{
			echo("DROP TABLE IF EXISTS users");
		}
		query{
			echo("CREATE TABLE users(sNo INT,datetimeField datetime,myTimestamp timestamp)");
		}
		query{
			echo("INSERT INTO users(sNo,datetimeField,myTimestamp)VALUES(123,'2018-04-10 02:50:30','2018-04-10 02:50:30'),(345,'2018-07-12 08:30:30','2018-03-03 04:50:30')");
		}
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}
