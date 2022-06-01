component {

	mySQL = getCredentials();

	this.name = "luceedfdftefdfst";
	this.datasources["ldev2566_MYSQL"] = mySQL;
	this.datasource = "ldev2566_MYSQL";

	
	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo("DROP TABLE IF EXISTS LDEV2566_mysql");
		}
		query{
			echo("CREATE TABLE LDEV2566_mysql( emp_id int primary key auto_increment, emp_name varchar(20), emp_age int)");
		}
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV2566_mysql");
		}
	}
}