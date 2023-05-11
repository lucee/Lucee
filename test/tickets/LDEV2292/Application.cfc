component {

	this.name	=	Hash( GetCurrentTemplatePath() );
	this.sessionManagement 	= false;

	dbname	= 'LDEV2292';
	
	this.datasources[dbname] = server.getDatasource( "h2", server._getTempDir( "LDEV2292" ) );
	this.datasource = dbname;

	public function onRequestStart() {
		setting requesttimeout=10;
		query{
			echo("DROP TABLE IF EXISTS LDEV2292");
		}
		query{
			echo("CREATE TABLE LDEV2292( id int, name varchar(20))");
		}
		query{
			echo("INSERT INTO LDEV2292 VALUES( '1', 'lucee' )");
		}
		query{
			echo("INSERT INTO LDEV2292 VALUES( '2', 'railo' )");
		}
	}


}