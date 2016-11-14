component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "query scope in caller hides variables scope in CFC", function() {
			it('Creating cfc instance with new()',  function( currentSpec ) {
				uri=createURI("LDEV1059/index.cfm");
				local.result=_InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				assertEquals("",left(result.filecontent.trim(), 100));
			});
			it('Creating cfc instance with CreateObject()',  function( currentSpec ) {
				uri=createURI("LDEV1059/index.cfm");
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