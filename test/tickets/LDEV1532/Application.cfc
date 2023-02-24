component {
	this.name = "test1532";
	this.datasource = "LDEV1532";
	this.datasources["LDEV1532"] = server.getDatasource("mssql");

	function onRequestStart() {
		query{
			echo("DROP TABLE IF EXISTS LDEV1532");
		}
		query{
			echo("CREATE TABLE LDEV1532(id int, name varchar(255))");
		}
		query{
			echo("INSERT INTO LDEV1532 VALUES (1, 'test')");
		}
	}
}