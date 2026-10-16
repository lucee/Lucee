component {
	this.name="ldev3324_csrf_cfml_cache";
	this.sessionManagement = true;
	this.sessionStorage="cfml_session_rotate_cache_ram";
	this.sessionCluster = true;
	this.sessiontimeout="#createTimeSpan(0,0,0,1)#";
	this.setclientcookies="yes";
	this.applicationtimeout="#createTimeSpan(0,0,0,10)#";
	this.sessionType="cfml";
	this.cache.connections[ "cfml_session_rotate_cache_ram" ] = {
		class: "lucee.runtime.cache.ram.RamCache",
		storage: true,
		custom: { timeToLiveSeconds: 600, timeToIdleSeconds: 0 }
	};
}
