component extends="org.lucee.cfml.test.LuceeTestCase" labels="ORM" skip="true"{

	function beforeAll() {
		variables.uri = createURI("LDEV4185");
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV4185", function() {
			it( title="Checking isWithinTransaction() with native Hibernate transaction", skip="#notHasH2()#", body=function( currentSpec ) {
				var result = _InternalRequest(
					template : "#variables.uri#/LDEV4185.cfm"
				);
				expect(trim(result.filecontent)).toBeTrue();
			});
		});
	}

	private boolean function notHasH2() {
		variables.dbfile = "#getDirectoryFromPath( getCurrentTemplatePath() )#LDEV4185/datasource/db";
		return !structCount(server.getDatasource("h2", variables.dbfile));
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}