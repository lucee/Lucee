component {

	this.name = "LDEV-6129-custom";
	this.datasources["LDEV6129h2"] = server.getDatasource( "h2", server._getTempDir( "LDEV6129custom" ) );
	this.datasources["LDEV6129h2"]["connectionLimit"] = 1;
	this.datasources["LDEV6129h2"]["maxTotal"] = 1;
	this.ormEnabled = true;
	this.datasource = "LDEV6129h2";
	this.ormSettings = {
		dbcreate: "dropcreate",
		dialect: "h2",
		flushAtRequestEnd: true,
		autoManageSession: true,
		hibernateConfig: {
			"connection.release_mode": "after_transaction",
			"hibernate.connection.provider_class": extensionExists( "D062D72F-F8A2-46F0-8CBC91325B2F067B" )
				? "ortus.extension.orm.jdbc.ConnectionProviderImpl"
				: "org.lucee.extension.orm.hibernate.jdbc.ConnectionProviderImpl"
		}
	};

	public function onRequestStart() {
		setting requesttimeout = 10;
	}

}
