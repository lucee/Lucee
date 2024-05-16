component {
	this.name="LDEV1995";
	this.functionPaths = getDirectoryFromPath(getCurrentTemplatePath())&"testFunction\" ;

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}
