component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	function run( testResults , testBox ) {
		describe( "Testing imageResize()", function() {
			it('Testing imageResize() with blank height',  function( currentSpec ) {
				uri=createURI("LDEV0585/test.cfm");
				local.result=_InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				assertEquals("128", left(result.filecontent.trim(), 100));
			});
			it('Testing imageResize() without height',  function( currentSpec ) {
				uri=createURI("LDEV0585/test.cfm");
				local.result=_InternalRequest(
					template:uri,
					forms:{Scene=2}
				);
				assertEquals("128", left(result.filecontent.trim(), 100));
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}