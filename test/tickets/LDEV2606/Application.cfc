component {

	msSQL = getDatasource();
	
	this.name = "Luceetest";
	this.datasources["LDEV2606_DSN"] = msSQL
	this.datasource = "LDEV2606_DSN";

	public function onRequestStart(){
		if( StructIsEmpty(mySQL) ){
			writeoutput("Datasource credentials was not available"); // Datasource credentials was not available means need to skip the iteration.
			abort;
		}
		query{
			echo("DROP TABLE IF EXISTS LDEV2606");
		}
		query{
			echo("CREATE TABLE LDEV2606( id INT PRIMARY KEY, value BIT)");
		}
		query{
			echo("INSERT INTO LDEV2606 VALUES( '1','false')");
			echo("INSERT INTO LDEV2606 VALUES( '2','false')");
		}
		
	}	
	
	private struct function getDatasource() {
		return server.getDatasource("mysql");
	}

	public function onRequestEnd(){
		if( StructIsEmpty(mySQL) ) return;
		query{
			echo("DROP TABLE IF EXISTS LDEV2606");
		}
	}
}