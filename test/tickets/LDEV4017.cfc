component extends="org.lucee.cfml.test.LuceeTestCase" labels="ORM" {

	function beforeAll() {
		variables.uri = createURI("LDEV4017");
	}

	function afterAll() {
		cleanup();
	}

	private function cleanUp() {
		if (!notHasH2()) {
			queryExecute( sql="DROP TABLE IF EXISTS persons", options: {
				datasource: server.getDatasource("h2", variables.dbfile)
			}); 

			queryExecute( sql="DROP TABLE IF EXISTS thoughts", options: {
				datasource: server.getDatasource("h2", variables.dbfile)
			}); 
		}
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV4017", function() {
			it( title="", skip="#notHasH2()#", body=function( currentSpec ) {
				var result = _InternalRequest(
					template : "#variables.uri#/LDEV4017.cfm",
					forms : { uuid : createUUID(), dbfile : variables.dbfile }
				);
				expect(trim(result.filecontent)).toBe("person.hasthoughts: true & lazy-loaded works outside of transcation");
			});
		});
	}

	private boolean function notHasH2() {
		variables.dbfile = "#getDirectoryFromPath( getCurrentTemplatePath() )#/datasource/db";
		return !structCount(server.getDatasource("h2", variables.dbfile));
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}