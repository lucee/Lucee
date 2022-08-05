component {

	this.name	=	Hash( GetCurrentTemplatePath() );
	this.sessionManagement 	= false;

	dbname	= 'LDEV2292';
	dbpath	= expandPath("#getTempDirectory()#/data/#dbname#");

	this.datasources[dbname] = {
		  class: 'org.h2.Driver'
		, bundleName: 'org.h2'
		, bundleVersion: '1.3.172'
		, connectionString: 'jdbc:h2:#dbpath#;MODE=MySQL'
		, connectionLimit:100 // default:-1
	};
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