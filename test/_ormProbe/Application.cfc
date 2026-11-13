component {

	this.name = "lucee-test-orm-probe";
	this.datasources[ "ormProbeH2" ] = server.getTestService( "h2", server._getTempDir( "ormProbe" ) );
	this.datasource = "ormProbeH2";
	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate: "dropcreate",
		dialect: "h2",
		cfclocation: getDirectoryFromPath( getCurrentTemplatePath() ),
		flushAtRequestEnd: false,
		autoManageSession: false
	};

}
