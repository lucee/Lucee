component extends="org.lucee.cfml.test.LuceeTestCase" labels="transaction" skip=true {
	
	function run( testResults, testBox ) {
		describe("Testcase for LDEV4056", function() {
			it( title="Checking transaction with Exclusive connections for request option enabled", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#createURI("LDEV4056")#\LDEV4056.cfm"
				).filecontent;
				expect(trim(result)).toBe("success");
			});
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}