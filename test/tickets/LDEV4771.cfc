component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV4771");
	}	
	function run( testResults , testBox ) {
		describe( "test case for LDEV-4771", function() {
			it( title="checking to call a function with cfloop query", body=function( currentSpec ) {
				var result = _InternalRequest(
					template : "#uri#\LDEV4771.cfm"
				);
				expect( result.filecontent ).toBe("2221");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}