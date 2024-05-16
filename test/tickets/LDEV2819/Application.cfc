component {

	this.name = "LDEV2819";
	this.datasources["LDEV2819_DSN"] = server.getDatasource("mssql");
	this.datasource = "LDEV2819_DSN";

	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo( "DROP TABLE IF EXISTS LDEV2819" );
		}
		query{
			echo( "CREATE TABLE LDEV2819( id int identity primary key, price int, Edition int)" );
		}
	}	
		
	public function onRequestEnd(){
		query{
			echo( "DROP TABLE IF EXISTS LDEV2819" );
		}
	}
}
