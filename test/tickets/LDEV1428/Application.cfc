
component {
	this.name 	= "LDEV1428" & hash( getCurrentTemplatePath() );
	this.datasource={
  		class: 'org.h2.Driver'
  		, bundleName: 'org.h2'
		, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db1428;MODE=MySQL'
	};

	this.ormEnabled = true;
	this.ormSettings = {
		savemapping=true,
		dbcreate = "update",
		secondarycacheenabled = false,
		logSQL 				= true,
		flushAtRequestEnd 	= false,
		autoManageSession	= false,
		skipCFCWithError	= false
	};

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
