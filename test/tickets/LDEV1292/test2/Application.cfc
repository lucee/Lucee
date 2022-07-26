component {
	this.name = "test";
	mySQL= getCredentials();
	this.datasource = {
		 type: "mysql"
		 ,host: "#mySQL.server#"
		 ,port: "#mySQL.port#"
		 ,database: "#mySQL.database#"
		 ,username: "#mySQL.username#"
		 ,password: "#mySQL.password#"
		 ,custom: { useUnicode:true }
	};
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
}