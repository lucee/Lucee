component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {
	function run( testResults, testBox ) {
		describe( title="Testcase for LDEV-4409", body=function() {
			it( title = "Checking numberFormat() with mask argument", body=function( currentSpec ) {
				try {
					var result = _InternalRequest(
						template:createURI("LDEV4409/test4409.cfm")
					).fileContent;
				}
				catch(any e) {
					var result = e.message;
				}
				assertEquals('1.23', result);
			});
		});
	}
	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()), "\/")#/";
		return baseURI & "" & calledName;
	}
}
