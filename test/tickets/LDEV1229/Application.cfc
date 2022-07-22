component {

	this.name	=	'test';
	this.sessionManagement 	= false;
	
	if (url.db=='h2') {
		this.datasource = server.getDatasource("h2", "#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db" );
	} else {
		mySQL = getCredentials();
		if(mySQL.count()!=0){
			this.datasource=server.getDatasource("mysql");
		}
	}

	// ORM settings
	this.ormEnabled = true;
	this.ormSettings = {
		autoManageSession = false
		,flushAtRequestEnd = false
		,dialect = "MySQLwithInnoDB"
	};
	
	public function onRequestStart() {
		setting requesttimeout=10;
		if(url.db=='mysql') {
			query {
		        echo("SET FOREIGN_KEY_CHECKS=0");
			}
		}
		query {
	        echo("DROP TABLE IF EXISTS `child`");
		}
		query {
	        echo("DROP TABLE IF EXISTS `parent`");
		}
		query {
	        echo("CREATE TABLE `child` (
	          `ID` int(10) unsigned NOT NULL,
	          `parentID` int(10) unsigned DEFAULT NULL,
	          PRIMARY KEY (`ID`)
	        );");
		}
		query {
	        echo("CREATE TABLE `parent` (
				  `ID` int(10) unsigned NOT NULL,
				  PRIMARY KEY (`ID`)
				);");
		}
		query {
	        echo("INSERT INTO `parent` VALUES ('1');");
		}
		if(url.db=='mysql') {
			query {
	        	echo("SET FOREIGN_KEY_CHECKS=1");
			}
		}
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
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