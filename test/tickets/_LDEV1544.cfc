component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV1544");
		if(!directoryExists("#variables.uri#")) directoryCreate("#variables.uri#");
		fileWrite("#variables.uri#/Test.cfc",'component {}');
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1544", function() {
			it( title='Checking exception message in response header, while internal server error', body=function( currentSpec ) {
			 	cfhttp(url="http://#CGI.SERVER_NAME#/test/testcases/LDEV1544/test.cfc?method=withoutFunction") {
				}
				expect(structKeyExists(cfhttp.responseheader, "exception-message")).toBe(false);
			});

			it( title='Checking exception message in response header, while 404 not found error', body=function( currentSpec ) {
			 	cfhttp(url="http://#CGI.SERVER_NAME#/test/testcases/LDEV1544/tests.cfc?method=withoutFunction") {
				}
				expect(structKeyExists(cfhttp.responseheader, "exception-message")).toBe(false);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}