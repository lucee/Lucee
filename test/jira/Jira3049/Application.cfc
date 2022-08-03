component {
	this.name = hash( getCurrentTemplatePath() );
	this.ormEnabled="true";
    this.ormSettings = {
		dbcreate="update",
		cfcLocation="orm",
		savemapping=true
    };
    
    this.datasource = {
		class: 'org.h2.Driver'
		, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db;MODE=MySQL'
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