component {
	this.name = "ldev-6939-" & hash( getCurrentTemplatePath() );
	this.sessionManagement = true;
	this.setClientCookies = true;
	this.sessionType = "application";
	this.sessionTimeout = createTimeSpan( 0, 0, 5, 0 );
	this.cache.connections[ "ldev6939cache" ] = {
		class: "lucee.runtime.cache.ram.RamCache",
		storage: true,
		custom: { timeToLiveSeconds: 0, timeToIdleSeconds: 0 }
	};
	this.sessionStorage = "ldev6939cache";

	function onSessionStart() {
		session.user = new User();
	}
}
