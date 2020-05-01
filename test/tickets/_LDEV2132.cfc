component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll(){
		variables.uri = createURI("LDEV2132");
	}

	function run( testResults , testBox ) {
		describe( "Test case for LDEV-2132", function() {
			it( title = "java.io.IOException with Tomcat 9", body = function( CurrentSpec ) {
				local.result = _InternalRequest(
					template : "#variables.uri#/test.cfm"
				);
				expect(trim(result.filecontent)).tobe(true);
			});
		});
	}

	private string function createURI( string calledName ){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}