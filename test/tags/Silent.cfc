component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.uri = createURI("cfsilent");
	}
	
	function run( testResults , testBox ) {
		describe( "Testcase for cfsilent", function() {
			it( title = "checking cfsilent", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\cfsilent.cfm",
					forms : { scene = 1 }
				).filecontent;
				expect(trim(result)).toBe("cfsilent outside");
			});
			
			it( title = "checking cfsilent with bufferoutput=true", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\cfsilent.cfm",
					forms : { scene = 2 }
				).filecontent;
				expect(trim(result)).toBe("bufferoutput=true error");
			});
			
			it( title = "checking cfsilent with bufferoutput=false", body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#\cfsilent.cfm",
					forms : { scene = 3 }
				).filecontent;
				expect(trim(result)).toBe("error");
			});
		});
	}
	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
} 