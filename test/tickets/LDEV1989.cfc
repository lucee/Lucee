component extends="org.lucee.cfml.test.LuceeTestCase" labels="zip"{
	function beforeAll(){
		variables.uri = createURI("LDEV1989");
	}
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1989", function() {
			it( title='Checking password and empty encryptionAlgorithm in CFZIP', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/zip-password-test.cfm",
					url: {
						encryptionAlgorithm: ""
					}
				);
				expect(result.filecontent.trim()).toBe("true");
			});

			it( title='Checking password and encryptionAlgorithm=standard in CFZIP', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/zip-password-test.cfm",
					url: {
						encryptionAlgorithm: "standard"
					}
				);
				expect(result.filecontent.trim()).toBe("true");
			});

			it( title='Checking password and encryptionAlgorithm=aes in CFZIP', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/zip-password-test.cfm",
					url: {
						encryptionAlgorithm: "aes"
					}
				);
				expect(result.filecontent.trim()).toBe("true");
			});

			it( title='Checking password and encryptionAlgorithm=aes128 in CFZIP', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/zip-password-test.cfm",
					url: {
						encryptionAlgorithm: "aes128"
					}
				);
				expect(result.filecontent.trim()).toBe("true");
			});
		});
	}

	function afterAll(){
		var dir= getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV1989/zip";
		if ( directoryExists( dir) ) 
			directoryDelete( dir, true );
	}


	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
		return baseURI&""&calledName;
	}
} 