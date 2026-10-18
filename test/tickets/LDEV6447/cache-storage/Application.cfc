component {
	this.name = "ldev6447_cache_storage";
	this.sessionManagement = true;
	this.sessionType = "cfml";
	this.sessionStorage = "ldev6447_session_ram";
	this.sessionCluster = url.sessionCluster ?: false;
	this.sessionTimeout = createTimeSpan( 0, 0, 0, 30 );
	this.setClientCookies = true;
	this.applicationTimeout = createTimeSpan( 0, 0, 1, 0 );
	this.cache.connections[ "ldev6447_session_ram" ] = {
		class: "lucee.runtime.cache.ram.RamCache",
		storage: true,
		custom: { timeToLiveSeconds: 600, timeToIdleSeconds: 0 }
	};
}
