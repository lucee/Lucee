component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Getting caches from Application.cfc", function() {
			it('Static member without access modifier',  function( currentSpec ) {
				uri=createURI("LDEV0600/index.cfm");
				local.result=_InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				assertEquals("",left(result.filecontent.trim(), 100));
			});
			it('Static member with access modifier',  function( currentSpec ) {
				uri=createURI("LDEV0600/index.cfm");
				local.result=_InternalRequest(
					template:uri,
					forms:{Scene=2}
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