component {
	this.name = "OSGiNotInstalled";
	this.cache.connections[ "osgiguard" ] = {
		class: "lucee.osgiguard.Cache",
		bundleName: "lucee.osgiguard.cache",
		bundleVersion: "1.0.0",
		storage: false,
		custom: {},
		default: ""
	};
}
