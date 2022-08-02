component {
	this.name =	Hash( GetCurrentTemplatePath() ) & "2s";
	this.tag["query"].cachedwithin = "#createTimeSpan(0,0,1,0)#";
	this.tag["object"].cachedwithin = "#createTimeSpan(0,0,1,0)#";
	this.tag["include"].cachedwithin = "#createTimeSpan(0,0,1,0)#";
	this.tag["function"].cachedwithin = "#createTimeSpan(0,0,1,0)#";
	mySQL = getCredentials();
	this.datasource = mySQL;

	public function onRequestStart() {
		setting requesttimeout=10 showdebugOutput=false;
	}

	function onApplicationStart(){
		query{
			echo("DROP TABLE IF EXISTS details");
		}
		query{
			echo("CREATE TABLE details( id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(30) )");
		}
		query{
			echo("INSERT INTO details VALUES(1,'testUserDetails')");
		}
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}
