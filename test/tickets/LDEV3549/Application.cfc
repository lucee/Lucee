component {
    this.name = "LDEV-3549";
    this.javasettings = { loadPaths:"#getDirectoryFromPath(getCurrentTemplatePath())#jar", reloadOnChange:true};

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}