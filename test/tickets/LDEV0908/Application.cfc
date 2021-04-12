component {
	this.name =	"LDEV-908";
	this.datasource ={
	  		class: 'org.h2.Driver'
	  		, bundleName: 'org.h2'
			, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db908;MODE=MySQL'
		};


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

	/*private struct function getCredentials() {
		return server.getDatasource("mysql");
	}*/
}
