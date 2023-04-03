component {
	this.name = "LDEV-4044";
	this.datasource = server.getDatasource("mysql");

	function onRequestStart() {
		query {
			echo("CREATE TABLE IF NOT EXISTS LDEV4044 (
				id INT,
				name VARCHAR(50)
			)");
		}
	}

	function onRequestEnd() {
		query {
			echo("DROP TABLE IF EXISTS LDEV4044");
		}
	}
} 