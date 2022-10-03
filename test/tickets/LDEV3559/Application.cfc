component {
	this.name = "test";

	this.datasource = server.getDatasource("mssql");

	function onRequestStart() {
		query {
			echo("DROP TABLE IF EXISTS LDEV3559");
		}
		query {
			echo("CREATE TABLE LDEV3559 (Height decimal (3,2))");
		}
		query {
			echo("INSERT INTO LDEV3559 VALUES(5.0), (6.0)");
		}
	}

	function onRequestEnd() {
		query {
			echo("DROP TABLE IF EXISTS LDEV3559");
		}
	}
} 