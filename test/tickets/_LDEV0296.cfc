component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Getting caches from Application.cfc", function() {
			it('Checking initmethod attribute for component',  function( currentSpec ) {
				uri=createURI("LDEV0296/index.cfm");
				local.result=_InternalRequest(
					template:uri
				);
				assertEquals("40",left(result.filecontent.trim(), 100));
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}