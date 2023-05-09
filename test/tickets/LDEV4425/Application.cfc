component {
	this.name = "test4425";
	this.datasource = server.getDatasource("mssql");

	public function onRequestStart() {
		query{
			echo("DROP TABLE IF EXISTS LDEV4425");
		}
		query{
			echo("CREATE TABLE LDEV4425( id int NOT NULL IDENTITY PRIMARY KEY, test VARCHAR(50))");
		}
	}
}