component {

	this.name	=	Hash( GetCurrentTemplatePath() ) & "2s";
	this.sessionManagement 	= false;

	this.datasource =  server.getDatasource( "h2", server._getTempDir( "LDEV0374" ) );

	// ORM settings
	this.ormEnabled = true;
	this.ormSettings = {
		// dialect = "MySQLwithInnoDB",
		// dialect = "MicrosoftSQLServer",
		dbcreate="dropcreate"
	};

	function onApplicationStart(){
		try{
			query {
				echo("DROP TABLE users");
			}
		}
		catch(local.e) {}

		query{
			echo("CREATE TABLE users( 
				id INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
				DateJoined DATETIME )");
		}

	}

	
	public function onRequestStart() {
		setting requesttimeout=10 showdebugOutput=false;
	}
}