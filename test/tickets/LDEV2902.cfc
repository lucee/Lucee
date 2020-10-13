component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll(){
		variables.uri = createURI("LDEV2902");
	}

	function run( testResults, testBox ) { 
		describe( "testcase for LDEV-2902", function(){
			it(title="Checking datasource configured with timezone",body=function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 1}
				);
				expect(trim(result.filecontent)).toBe("America/Chicago");
			});

			it(title="Checking datasource configured without timezone",body=function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 2}
				);
				expect(trim(result.filecontent)).toBe("key [TIMEZONE] doesn't exist");
			});

			it(title="Checking datasource configured Empty timezone",body=function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : {Scene = 3}
				);
				expect(trim(result.filecontent)).toBe("key [TIMEZONE] doesn't exist");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}