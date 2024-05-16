component {
	this.name = Hash( GetCurrentTemplatePath() );
    request.baseURL="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath=GetDirectoryFromPath(getCurrentTemplatePath());
    path = getDirectoryFromPath(getCurrenttemplatepath());

	this.javasettings={
    	LoadPaths = [path&'lib/ldev1121.jar'], 
    	loadColdFusionClassPath = true, 
    	reloadOnChange= true, 
    	watchInterval = 10000, 
    	watchExtensions = "jar,class,xml"
	};

	public function onRequestStart() {
		setting requesttimeout=10;
	}
}