component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1995", body=function() {
			it( title='checking this.functionpaths with string and struct function',body=function( currentSpec ) {
				var uri = createURI("LDEV1995");
				local.result = _InternalRequest(
					template:"#uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('testString||true');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
