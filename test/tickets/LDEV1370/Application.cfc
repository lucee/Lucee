component {
	this.name 				= "LDEV1370" & hash( getCurrentTemplatePath() );

	this.datasource={
	  		class: 'org.h2.Driver'
	  		, bundleName: 'org.h2'
			, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db1370;MODE=MySQL;MVCC=true'
		};

	this.ormEnabled = true;
	this.ormSettings = {
		dbcreate = "update",
		secondarycacheenabled = false,
		//flushAtRequestEnd 	= true,
		//autoManageSession	= true,
		secondaryCacheEnabled = false,
		eventhandling = true
	};

	if(!isNull(url.flushAtRequestEnd)) this.ormSettings.flushAtRequestEnd=url.flushAtRequestEnd;
	if(!isNull(url.autoManageSession)) this.ormSettings.autoManageSession=url.autoManageSession;
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

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