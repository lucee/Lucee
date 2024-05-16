component {
	this.name	=	'test';
	this.customtagpaths["/1276"]="#getDirectoryFromPath(getCurrenttemplatepath())#/lib";
// "Inspect Templates (CFM/CFC)"/"Web Accessible" setting not supported with application type mappings

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}