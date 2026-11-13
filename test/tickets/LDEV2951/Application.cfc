component {
	this.name = "LDEV2951-" & hash( getCurrentTemplatePath() );

	this.cache.connections[ "ldev2951app" ] = {
		class: "lucee.runtime.cache.ram.RamCache"
		, storage: false
		, custom: { "timeToIdleSeconds": "0", "timeToLiveSeconds": "0" }
		, default: ""
	};
}
