component {
	this.name = "orm" & hash( getCurrentTemplatePath() );

	param name="form.dbfile" default=""; // required for h2 
	param name="form.db" default=""; // 

	switch(form.db){
		case "h2":
			// H2 config - gives me a lock timeout error
			this.datasource= server.getDatasource("h2", form.dbfile);
			this.ormEnabled = true;
			this.ormSettings = {
				dbcreate = "dropcreate"
			};
			break;
		case "mssql":
			// mssql setup (HANGS 5.4, passes 3.55)
			this.datasource = server.getDatasource("mssql");
			this.ormEnabled = true;
			this.ormSettings = {
				dbcreate = "dropcreate",
				dialect  = "org.hibernate.dialect.SQLServer2008Dialect"
			};
			break;
		default:
			throw "db #form.db# no yet supported";
	}

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}