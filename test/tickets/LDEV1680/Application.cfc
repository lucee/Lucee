component {
	this.name = "test";
	this.datasource = server.getDatasource("mssql");

	function onRequestStart() {
		query {
			echo("DROP TABLE IF EXISTS LDEV1680");
		}
		query {
			echo("CREATE TABLE LDEV1680 (
				dateTimeoff_column DATETIMEOFFSET
			)");
		}
		query {
			echo("INSERT INTO LDEV1680 VALUES ('01/01/2022 10:10:10')");
		}
	}

	function onRequestEnd() {
		query {
			echo("DROP TABLE IF EXISTS LDEV1680");
		}
	}
} 