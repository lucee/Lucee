component {
	this.name =	"LDEV-908";
	this.datasource = server.getDatasource( "h2", server._getTempDir( "LDEV0908" ) );

	this.ormenabled= true ;

	function onApplicationStart(){
		query{
			echo("DROP TABLE IF EXISTS LDEV0908");
		}


		query{
			echo("CREATE TABLE LDEV0908(ID INT NOT NULL AUTO_INCREMENT,col1 VARCHAR(50), col2 VARCHAR(50),PRIMARY KEY (ID))");
		}

		query{
			echo("INSERT INTO LDEV0908(col1,col2) VALUES ('test1','test2')");
		}
	}

	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

	/*private struct function getCredentials() {
		return server.getDatasource("mysql");
	}*/

}
