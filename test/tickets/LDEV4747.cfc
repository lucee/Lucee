component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function beforeAll() {
		variables.uri = createURI("LDEV4747");
		if ( !directoryExists( uri ) ) {
			directoryCreate( uri );
		}
	}

	function run( testResults, testBox ) {
		describe( title = "Testcase for LDEV-4747", body = function() {
			it(title = "Testcase for DirectoryList() function", body = function( currentSpec ) {
				fileWrite(uri&"/00001_RenameTbl_G4Agent.sql", "");
				fileWrite(uri&"/00002_CreateTbl_G4AgentSite.sql", "");
				fileWrite(uri&"/00002a_AddData_G4AgentSite.sql", "");
				buildFiles = DirectoryList(path=uri, recurse=false, listInfo=false, type="file", sort='name asc',
				filter=function(thefile) {
					return Left(GetFileFromPath(thefile), 1) neq 'z';
				}
				);
				expect(buildfiles[1]).toInclude("00001_RenameTbl_G4Agent.sql")
				expect(buildfiles[2]).toInclude("00002_CreateTbl_G4AgentSite.sql")
				expect(buildfiles[3]).toInclude("00002a_AddData_G4AgentSite.sql")
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI & "" & calledName;
	}

	function afterAll() {
		if ( directoryExists(uri) ) {
			directoryDelete( uri, true );
		}
	}
}
