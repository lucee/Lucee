component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function beforeAll() {
		variables.dir = getDirectoryFromPath(getCurrentTemplatePath()) & "LDEV982";
		variables.uri = createURI("LDEV982");
		variables.target = dir & "/test.cfm";
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-982 ", function() {
			it(title="application scope shouldn't expire inside a long running request", body=function( currentSpec ){
				
				expect(function(){
					res = _internalRequest(
						template = "#variables.uri#/index.cfm",
						url : {
							SERVERADMINPASSWORD: request.SERVERADMINPASSWORD
						}
					);
				}).NotToThrow();
				expect( res.fileContent.trim() ).toBe( "truetrue" );
			});
			
		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
