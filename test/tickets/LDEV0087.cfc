component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "ORM persistent false for inherited property", function() {
			it('With dbtype hql',  function( currentSpec ) {
				uri=createURI("LDEV0087/index.cfm");
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