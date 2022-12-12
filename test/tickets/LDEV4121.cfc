component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" skip="true"{
	function beforeAll() {
		variables.uri = createURI("LDEV4121");
	}

	function afterAll() {
		cleanup();
	}

	private function cleanUp() {
		if (!notHasH2()) {
			queryExecute( sql="DROP TABLE IF EXISTS LDEV4121", options: {
				datasource: server.getDatasource("h2", variables.dbfile)
			}); 
		}
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-4121", function() {
			it( title="checking default property value to overide NULL value on ORM Entity",skip="#notHasH2()#",   body=function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\LDEV4121.cfm"
				);
				expect(trim(result.filecontent)).toBe("default organization name");
			});
		});
	}

	private boolean function notHasH2() {
		variables.dbfile = "#getDirectoryFromPath( getCurrentTemplatePath() )#/datasource/dbh2";
		return !structCount(server.getDatasource("h2", variables.dbfile));
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}