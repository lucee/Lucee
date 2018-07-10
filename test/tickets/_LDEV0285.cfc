component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-285", function() {
			it(title="checking Lucee parsing, with coldfusion tag in commented line before 'cfscript' tag", body = function( currentSpec ) {
				var uri=createURI("LDEV0285/App1.cfc");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("");
			});

			it(title="checking Lucee parsing, with coldfusion tag in commented line inside 'cfscript' tag", body = function( currentSpec ) {
				var uri=createURI("LDEV0285/App2.cfc");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("");
			});

			it(title="checking Lucee parsing, with coldfusion tag in commented line in tag based component", body = function( currentSpec ) {
				var uri=createURI("LDEV0285/App3.cfc");
				var result = _InternalRequest(
				template:uri
				);
				expect(result.filecontent.trim()).toBe("");
			});
		});
	}
	// private Function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
