component {
	this.name = Hash( GetCurrentTemplatePath() );
    request.baseURL="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath=GetDirectoryFromPath(getCurrentTemplatePath());
    path = getDirectoryFromPath(getCurrenttemplatepath());

	this.javasettings={
    	LoadPaths = ["#path#"], 
    	loadColdFusionClassPath = true, 
    	reloadOnChange= true, 
    	watchInterval = 100, 
    	watchExtensions = "jar,class,xml"
	};
}