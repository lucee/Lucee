component {
    this.name = "LDEV-3549";
	testJarPath = url.tempJarFolder & "/test.jar";
	fileCopy( getDirectoryFromPath(getCurrentTemplatePath()) & "test.jar", testJarPath );
    this.javasettings = { 
		loadPaths: testJarPath, 
		reloadOnChange: true
	};

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}