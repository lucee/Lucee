component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV2754");
	}

	function run( testResults, testBox ){
		describe( "Test case for LDEV2754", function(){
			it(title = "Using (?) mark with DB", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { scene = 1 }
				)
				expect(result.filecontent).tobe("juwait");
			});

			it(title = "Using (') with DB ", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { scene = 2 }
				)
				expect(result.filecontent).tobe("juwait");
			});

			it(title = "Using (') with QoQ", body = function( currentSpec ){
				local.result = _InternalRequest(
					template : "#uri#\test.cfm",
					forms : { scene = 3 }
				)
				expect(result.filecontent).tobe("lucee");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}