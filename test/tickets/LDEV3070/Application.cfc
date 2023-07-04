component {
	this.name = "test";

	this.datasource = "LDEV3070";

	this.datasources["LDEV3070"] = server.getDatasource("mysql");

	function onRequestStart() {
		query {
			echo("CREATE TABLE IF NOT EXISTS LDEV3070( id int, name varchar(20))");
		}
	}
}