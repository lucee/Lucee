component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" skip="true"{
	function beforeAll() {
		variables.uri = createURI("LDEV4121");
	}

	function run( testResults, testBox ) {
		describe(title="Testcase for LDEV-4121", body=function() {
			it( title="checking default property value to override NULL value on ORM Entity", skip=(noOrm() || notHasH2()), body=function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\LDEV4121.cfm"
				);
				expect(trim(result.filecontent)).toBe("default organization name");
			});
		});
	}

	private boolean function notHasH2() {
		variables.dbfile = server._getTempDir( "LDEV4121" );
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