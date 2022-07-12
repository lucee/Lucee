component {

	this.name = "orm-events";
	this.datasource= server.getDatasource("h2", "#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db" );
	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate: "dropcreate",
		//dbCreate: "none",
		eventHandling: true,
		eventHandler: "eventHandler",
		autoManageSession: false,
		flushAtRequestEnd: true,
		useDBForMapping: false,
		dialect: "h2"
	};

	function onApplicationStart() {
		application.ormEventLog = [];
	}

	public function onRequestStart() {
		setting requesttimeout=10;
		application.ormEventLog = [];
		if ( url.keyExists( "flushcache" ) ){
			componentCacheClear();
		}
	}

	function onRequestEnd() {
		var javaIoFile=createObject("java","java.io.File");
		loop array = DirectoryList(
				path = getDirectoryFromPath( getCurrentTemplatePath() ), 
				recurse = true, filter="*.db") item="local.path"  {
			fileDeleteOnExit(javaIoFile,path);
		}
	}

	private function fileDeleteOnExit(required javaIoFile, required string path) {
		var file=javaIoFile.init(arguments.path);
		if(!file.isFile())file=javaIoFile.init(expandPath(arguments.path));
		if(file.isFile()) file.deleteOnExit();
	}

}