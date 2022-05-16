component {

	// SET APPLICATION MAPPINGS
	this.mappings["/"] = getDirectoryFromPath(getCurrentTemplatePath());

	request.outside_false = fileExists(expandPath("/index.cfm"), false);
	request.outside_true = fileExists(expandPath("/index.cfm"), true);

	public any function onRequestStart(string targetPath){
		setting requesttimeout=10;
		request.inside_false = fileExists(expandPath("/index.cfm"), false);
		request.inside_true = fileExists(expandPath("/index.cfm"), true);
	}

}