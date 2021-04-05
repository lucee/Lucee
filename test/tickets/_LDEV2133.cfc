component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.uri = createURI("LDEV2133");
	}
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2133", function() {
			it(title = "checking abort with type='page in cfscript'", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=1}
				);
				expect(local.result.filecontent.trim()).toBe('Page type');
			});

			it(title = "checking abort with type='request' in cfscript", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=2}
				);
				expect(local.result.filecontent.trim()).toBe('');
			});
			
			it(title = "checking cfabort with type='page'", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=3}
				);
				expect(local.result.filecontent.trim()).toBe('Page type');
			});

			it(title = "checking cfabort with type='request'", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=4}
				);
				expect(local.result.filecontent.trim()).toBe('');
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}