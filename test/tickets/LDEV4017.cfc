component extends="org.lucee.cfml.test.LuceeTestCase" labels="ORM" {

	function beforeAll() {
		variables.uri = createURI("LDEV4017");
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV4017", function() {
			it( title="Access the lazy-loaded ORM entity after the transaction ends", skip=(noOrm() || notHasH2()), body=function( currentSpec ) {
				var result = _InternalRequest(
					template : "#variables.uri#/LDEV4017.cfm",
					forms : { uuid : createUUID(), dbfile : variables.dbfile }
				);
				expect(trim(result.filecontent)).toBe("true & lazy-loaded");
			});
		});
	}

	private boolean function notHasH2() {
		variables.dbfile = server._getTempDir( "LDEV4017" );
		return !structCount(server.getDatasource("h2", variables.dbfile));
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	private function noOrm() {
		return ( structCount( server.getTestService("orm") ) eq 0 );
	}
}
