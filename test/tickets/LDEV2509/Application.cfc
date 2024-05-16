component {

	this.name = "Lucee";
	this.datasources["LDEV2509_DSN"] = server.getDatasource("mssql");

	this.datasource = "LDEV2509_DSN";

	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo("DROP TABLE IF EXISTS LDEV2509");
		}
		query{
			echo("CREATE TABLE LDEV2509( id int, Title SQL_VARIANT )");
		}
		query{
			echo("INSERT INTO LDEV2509 VALUES( 1,'Lucee' )");
		}
	}
}