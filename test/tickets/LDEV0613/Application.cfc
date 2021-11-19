component {
	this.name = hash( getCurrentTemplatePath() );
    request.baseURL="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#";
	request.currentPath=GetDirectoryFromPath(getCurrentTemplatePath());


 	this.datasource = {
	  class: 'org.h2.Driver'
		, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db;MODE=MySQL'
	};

	this.ormEnabled = true;
	this.ormSettings = {
		savemapping=true,
		dbcreate = 'dropcreate',
		logSQL=true
	};

	function onRequestEnd() {
		var javaIoFile=createObject("java","java.io.File");
		loop array=DirectoryList(
			path=getDirectoryFromPath(getCurrentTemplatePath()), 
			recurse=true, filter="*.db") item="local.path"  {
			fileDeleteOnExit(javaIoFile,path);
		}
	}

	private function fileDeleteOnExit(required javaIoFile, required string path) {
		var file=javaIoFile.init(arguments.path);
		if(!file.isFile())file=javaIoFile.init(expandPath(arguments.path));
		if(file.isFile()) file.deleteOnExit();
	}
}