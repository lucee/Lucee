component extends="org.lucee.cfml.test.LuceeTestCase" labels="zip"{
	function beforeAll(){
		variables.uri = createURI("LDEV1989");
	}
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1989", function() {
			it( title='Checking password attribute in CFZIP', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm");
				expect(result.filecontent.trim()).toBe("true");
			});
			it( title='Checking encryptionAlgorithm attribute in CFZIP', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test2.cfm");
				expect(result.filecontent.trim()).toBe("true");
			});
		});
	}

	function afterAll(){
		var dir=getDirectoryFromPath(getCurrenttemplatepath()) & "LDEV1989/zip";
		if (directoryExists( dir)) directoryDelete(dir,true);
	}


	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
} 