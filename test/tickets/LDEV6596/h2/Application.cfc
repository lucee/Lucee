component {
	this.name = "ldev6596-h2-cluster";
	this.sessionManagement = true;
	this.sessionTimeout = createTimeSpan( 0, 0, 30, 0 );
	// datasource storage hands back a deserialized copy on every load, like Redis/Memcached
	this.datasources.ldev6596 = server.getDatasource( service="h2", dbFile=server._getTempDir( "LDEV6596" ), options={ storage: true } );
	this.sessionStorage = "ldev6596";
	this.sessionCluster = true;

	function onSessionStart() {
		structClear( session );
		session.box = {};
	}
}
