component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1850", body=function() {
			it( title='checking cfpdf action="ddxfile" ', body=function( currentSpec ) {
				var uri = createURI("LDEV1850");
				var result = _InternalRequest(
					template:"#uri#/test.cfm"
				);
				expect(result.filecontent.trim()).toBe('successful');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}