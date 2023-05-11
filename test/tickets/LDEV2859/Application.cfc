component {

	this.Name = "LDEV2859";
	this.sessionManagement = false;
	this.ormEnabled = true;
	this.datasource = "LDEV2859";
	this.ormSettings = {
        dbCreate = "none",
        useDBForMapping = false,
        dialect = "MicrosoftSQLServer"
    };

	msSql = server.getDatasource("mssql");
	msSql.storage = true;
	
	this.datasources["LDEV2859"] = msSql;
	this.datasource = "LDEV2859";

	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo("DROP TABLE IF EXISTS test");
		}
		query{
			echo("CREATE TABLE test( id int, name varchar(20))");
		}
		query{
			echo("INSERT INTO test VALUES( '1', 'lucee' )");
			echo("INSERT INTO test VALUES( '2', 'railo' )");
		}
	}

	public function onRequestEnd(){
		query{
			echo("DROP TABLE IF EXISTS test");
		}
	}

}