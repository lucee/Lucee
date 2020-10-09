component extends = "org.lucee.cfml.test.LuceeTestCase" {
	
	function beforeAll(){
		variables.uri = createURI("LDEV3054");
	}

	function run( testresults , testbox ) {
		describe( "Testcase for LDEV-3054", function () {
			it( title = "cfapplication tag with enableNullSupport = 'true'",body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm"
				);
				expect(trim(isnull(result.filecontent))).toBe("true");
			});

			it( title = "cfapplication tag with enableNullSupport = 'false'",body = function ( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test1.cfm"
				);
				expect(trim(result.filecontent)).toBe("The key [T] does not exist.");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}