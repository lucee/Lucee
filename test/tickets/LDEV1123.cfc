component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1121", function() {
			it('checking custom tag with syntax error',  function( currentSpec ) {
				var uri=createURI("LDEV1123/redden.cfm");
				var result = _InternalRequest(
					template:uri
				);
				expect(result.filecontent.trim()).toInclude('Syntax Error, Invalid Construct');
			});
			
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}