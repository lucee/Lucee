component {
	this.name = "ldev2132-test";
	request.currentPath = GetDirectoryFromPath(getCurrentTemplatePath());
	this.javaSettings = {
		loadPaths = [request.currentPath&"commons-codec-1.9.jar"],
		loadColdFusionClassPath = true
	};

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}
