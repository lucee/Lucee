component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-865", function() {
			it(title="checking cfapplication tag, with attribute 'serversideFormValidation=true' in 'form' tag ", body = function( currentSpec ) {
				uri=createURI("LDEV0865/test1/index.cfm");
				result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("");
			});

			it(title="checking cfapplication tag, with attribute 'serversideFormValidation=false' in 'form' tag", body = function( currentSpec ) {
				uri=createURI("LDEV0865/test2/index.cfm");
				result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("");
			});

			it(title="checking cfapplication tag, with attribute 'serversideFormValidation=true' in 'cfform' tag", body = function( currentSpec ) {
				uri=createURI("LDEV0865/test3/index.cfm");
				result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("");
			});

			it(title="checking cfapplication tag, with attribute 'serversideFormValidation=false' in 'cfform' tag", body = function( currentSpec ) {
				uri=createURI("LDEV0865/test4/index.cfm");
				result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toBe("");
			});
		});
	}
	// private function//
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
