component {
	this.name = 'AppB';
	this.cache.connections["default"] = {
		 class: 'lucee.runtime.cache.ram.RamCache'
		, storage: true
		, custom: {"timeToIdleSeconds":"0","timeToLiveSeconds":"0"}
		, default: 'object'
	};
	this.cache.object = "default";
}