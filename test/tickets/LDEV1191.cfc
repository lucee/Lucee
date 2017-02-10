component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1191", function() {
			it( title='Checking query, include and function cache without using cachedwithin in application.cfc', body=function( currentSpec ) {
				uri=createURI("LDEV-1191/setCacheWithoutUsingApplication/index.cfm");
				local.result = _InternalRequest(
					template:uri
				);
				expect(local.result.filecontent.trim()).toBe("1|1|1");
			});

			it( title='Checking query, include and function cache with using cachedwithin in application.cfc', body=function( currentSpec ) {
				uri=createURI("LDEV-1191/setCacheUsingApplication/index.cfm");
				local.result = _InternalRequest(
					template:uri
				);
				expect(local.result.filecontent.trim()).toBe("1|1|1");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}