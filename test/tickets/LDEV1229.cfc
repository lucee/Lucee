component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {

		describe( title="Test suite for LDEV-1229 with mysql",  skip=checkMySqlEnvVarsAvailable(), body=function() {
			it(title="checking property tag, with the attribute cascade = 'all-delete-orphan' ", body = function( currentSpec ) {
				var uri=createURI("LDEV1229/index.cfm");
				var result = _InternalRequest(
					template:uri
					,urls:{db:'mysql'}
				);
				expect(result.filecontent.trim()).toBe(1);
			});
		});

		describe( title="Test suite for LDEV-1229 with h2",  body=function() {
			it(title="checking property tag, with the attribute cascade = 'all-delete-orphan' ", body = function( currentSpec ) {
				var uri=createURI("LDEV1229/index.cfm");
				var result = _InternalRequest(
					template:uri
					,urls:{db:'h2'}
				);
				expect(result.filecontent.trim()).toBe(1);
			});
		});

		afterTests();
	}
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private boolean function checkMySqlEnvVarsAvailable() {
		// getting the credentials from the environment variables
		var mySQL={};
		if(isNull(server.system)){
			server.system = structNew();
			currSystem = createObject("java", "java.lang.System");
			server.system.environment = currSystem.getenv();
			server.system.properties = currSystem.getproperties();
		}

		if(
			!isNull(server.system.environment.MYSQL_SERVER) &&
			!isNull(server.system.environment.MYSQL_USERNAME) &&
			!isNull(server.system.environment.MYSQL_PASSWORD) &&
			!isNull(server.system.environment.MYSQL_PORT) &&
			!isNull(server.system.environment.MYSQL_DATABASE)) {
			mySQL.server=server.system.environment.MYSQL_SERVER;
			mySQL.username=server.system.environment.MYSQL_USERNAME;
			mySQL.password=server.system.environment.MYSQL_PASSWORD;
			mySQL.port=server.system.environment.MYSQL_PORT;
			mySQL.database=server.system.environment.MYSQL_DATABASE;
		}
		// getting the credentials from the system variables
		else if(
			!isNull(server.system.properties.MYSQL_SERVER) &&
			!isNull(server.system.properties.MYSQL_USERNAME) &&
			!isNull(server.system.properties.MYSQL_PASSWORD) &&
			!isNull(server.system.properties.MYSQL_PORT) &&
			!isNull(server.system.properties.MYSQL_DATABASE)) {
			mySQL.server=server.system.properties.MYSQL_SERVER;
			mySQL.username=server.system.properties.MYSQL_USERNAME;
			mySQL.password=server.system.properties.MYSQL_PASSWORD;
			mySQL.port=server.system.properties.MYSQL_PORT;
			mySQL.database=server.system.properties.MYSQL_DATABASE;
		}
		return structIsEmpty(mySQL);
	}

	private function afterTests() {
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
