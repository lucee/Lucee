component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1641", body=function() {
			it(title="Checking ORM transaction, with larger id", body = function( currentSpec ) {
				var uri = createURI("LDEV1641/specs/test.cfm");
				var result = _InternalRequest(
					template:uri,
					forms:{Scene=1}
				);
				expect(result.fileContent.trim()).toBe("1|0|true");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}
