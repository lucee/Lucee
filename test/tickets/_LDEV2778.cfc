component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV2778");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV2778", function(){
			it(title="Rounded a number as dynamic", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { scene = 1 }
				)
				expect(trim(result.filecontent)).toBe(3.08);
			});
			it(title="Rounded a number as static", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { scene = 2 }
				)
				expect(trim(result.filecontent)).toBe(3.08);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}