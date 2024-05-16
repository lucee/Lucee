component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV2423");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV2423 ", function(){
			it(title = "cfqueryparam not working with CF_SQL_FLOAT for negative exponent numbers" ,body = function(){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 1 }
				);
				expect(trim(result.filecontent)).tobe("1");
			});

			it(title = "cfqueryparam not working with CF_SQL_NUMERIC for negative exponent numbers" ,body = function(){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { Scene = 2 }
				);
				expect(trim(result.filecontent)).tobe("0");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}