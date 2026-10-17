component {
	this.name = "ldev6594-csrf-cluster";
	this.sessionManagement = true;
	this.sessionTimeout = createTimeSpan( 0, 0, 30, 0 );
	this.cache.connections[ "ldev6594_sessions" ] = {
		class: "lucee.runtime.cache.ram.RamCache",
		storage: true,
		custom: { timeToIdleSeconds: 3600, timeToLiveSeconds: 0 }
	};
	this.sessionStorage = "ldev6594_sessions";
	this.sessionCluster = true;

	function onSessionStart() {
		structClear( session );
		session.startedAt = now();
	}
}
