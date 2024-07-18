component extends = "org.lucee.cfml.test.LuceeTestCase" {
	
	function beforeAll(){
		variables.uri = createURI("LDEV_3054");
	}	

	function run( testresults , testbox ) {
		describe( "Testcase for LDEV-3054", function () {
			it( title="cfapplication tag with enableNullSupport = 'true and false'",body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm"
				);
				expect(trim(result.filecontent)).toBe("");
				expect(find("true,The key [T]",trim(result.filecontent))>0 ).toBeTrue();
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}