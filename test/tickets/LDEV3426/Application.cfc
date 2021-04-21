component {
	pgSQL = getCredentials();
    this.name = "test3426";
    this.datasources["LDEV_3426"] = pgSQL;
	this.datasource = "LDEV_3426"
    public function onRequestStart(){
		query{
			echo("DROP TABLE IF EXISTS LDEV3426_Primary");
		}
        query{
			echo("DROP TABLE IF EXISTS LDEV3426_test");
		}
		query{
			echo("CREATE TABLE LDEV3426_Primary( id SERIAL NOT NULL PRIMARY KEY, test VARCHAR(50))");
		}
	}
    private struct function getCredentials() {
    	return server.getDatasource("postgres");
	}
    public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS LDEV3426_Primary");
		}
	}
}