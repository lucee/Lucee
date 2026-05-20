component {
	this.name = "ldev-5941-" & hash( getCurrentTemplatePath() );
	this.sessionManagement = true;
	this.setClientCookies = true;
	this.sessionType = "application";
	this.sessionTimeout = createTimespan( 0, 0, 5, 0 );
	this.applicationTimeout = createTimespan( 0, 1, 0, 0 );

	this.cache.connections[ "ldev5941cache" ] = {
		class: "lucee.runtime.cache.ram.RamCache",
		storage: true,
		custom: {
			timeToLiveSeconds: 300,
			timeToIdleSeconds: 0
		}
	};
	this.sessionStorage = "ldev5941cache";
}
