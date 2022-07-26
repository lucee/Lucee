component extends="org.lucee.cfml.test.LuceeTestCase" labels="pdf"{
	function beforeAll(){
		variables.uri = createURI("LDEV2611");
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-2611", body=function() {
			it(title = "check that output before is not suppressed", body = function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('.... any content');
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
} 

