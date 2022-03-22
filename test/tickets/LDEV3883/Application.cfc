component {

	if (isNull(url.type)) url.type = "";
	if (isNull(url.appName)) url.appName = "";

	this.name = "#createUUID()#";
	this.sessionManagement = true;
	this.sessionTimeout =createTimespan(0,0,0,10);
	this.datasource = {
		class: "org.h2.Driver"
		, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db;MODE=MySQL'
	};
	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate: "dropCreate",
		eventHandling: true,
		eventHandler: "event.eventHandler",
		dialect: "H2"
	};

	function onApplicationStart() {
		writeoutput("onApplicationStart executed,");
	}

	function onsessionStart() {
		writeoutput("onsessionStart executed,");
	}

	function onrequestStart() {
		writeoutput("onrequestStart executed,");
	}

	function onRequestEnd() {
		writeoutput("onRequestEnd executed");

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