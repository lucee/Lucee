component extends="org.lucee.cfml.test.LuceeTestCase" labels="search"{
	function beforeAll(){
		variables.uri = createURI("LDEV1901");
	}
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1901", function() {
			it( title='Checking criteria with ^ in cfsearch', body=function( currentSpec ) {
				include template="#variables.uri#/test.cfm";
				local.result = _InternalRequest(
					template:"#variables.uri#/test2.cfm");
				expect(result.filecontent.trim()).toBe("true");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
} 