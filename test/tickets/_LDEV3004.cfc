component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV3004");
	}

	function run( testResults , testbox ) {
		describe( "Testcase for LDEV3004",function (){
			it( title = "Default value without override",body = function ( currentSpecs ){
				local.result = _InternalRequest (
					template : "#uri#\test.cfm",
					form : { scene = 1 }
				);
			 	expect(result.filecontent).toBe("[default]");
			});
			
			it( title = "After override the default value",body = function ( currentSpecs ){
				local.result = _InternalRequest (
					template : "#uri#\test.cfm",
					form : { scene = 2 }
				);
				expect(result.filecontent).toBe("hello world!");
			});
		});
	}
	
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}