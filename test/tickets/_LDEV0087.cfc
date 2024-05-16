component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm"{
	function run( testResults , testBox ) {
		describe( "ORM persistent false for inherited property", function() {
			it('Case 1: This should be run without failures',  function( currentSpec ) {
				uri=createURI("LDEV0087/index.cfm");
				local.result=_InternalRequest(
					template:uri
				);
				assertEquals("",left(result.filecontent.trim(), 100));
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}