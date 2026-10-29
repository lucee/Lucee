component {
	this.name = "LDEV2951-" & hash( getCurrentTemplatePath() );

	this.cache.connections[ "ldev2951app" ] = {
		class: "lucee.runtime.cache.ram.RamCache"
		, storage: false
		, custom: { "timeToIdleSeconds": "0", "timeToLiveSeconds": "0" }
		, default: ""
	};

	// same name as a cache defined in the administrator by the test, the application cache must win
	this.cache.connections[ "ldev2951shared" ] = {
		class: "lucee.runtime.cache.ram.RamCache"
		, storage: false
		, custom: { "timeToIdleSeconds": "0", "timeToLiveSeconds": "222" }
		, default: ""
	};
}
