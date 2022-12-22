component {
	this.name = "LDEV-4337";
	this.datasource = server.getDatasource("mssql"); 
	
	function onRequestStart() {
		query {
			echo("DROP TABLE IF EXISTS LDEV4337");
		}
		query {
			echo("CREATE TABLE LDEV4337 (amount decimal(18,4))");
		}
	}

	function onRequestEnd() {
		query {
			echo("DROP TABLE IF EXISTS LDEV4337");
		}
	}
} 