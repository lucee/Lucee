component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function beforeAll() {
		variables.uri = createURI("LDEV3883");
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-3883", function() {
			it( title="check onApplicationStart - Using application scope in ORM event handler", body=function( currentSpec ) {    
				local.result = _internalRequest(
					template = "#uri#\LDEV3883.cfm",
					urls = { type:"handler", appName="test-3883_#createUniqueID()#" }
				)
				expect(trim(result.fileContent)).toBe("onApplicationStart executed,onSessionStart executed,onRequestStart executed,onRequestEnd executed");
			});

			it( title="check onApplicationStart - Using application scope in component", body=function( currentSpec ) {
				local.result = _internalRequest(
					template = "#uri#\LDEV3883.cfm",
					urls = { type:"cfc", appName="test-3883_#createUniqueID()#" }
				)
				expect(trim(result.fileContent)).toBe("onApplicationStart executed,onSessionStart executed,onRequestStart executed,onRequestEnd executed");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}