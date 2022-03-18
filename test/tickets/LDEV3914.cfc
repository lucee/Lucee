component extends="org.lucee.cfml.test.LuceeTestCase" skip="true"{
	
	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-3914", function() {
			it( title="checking with thread statement inside a closure function", body=function() {
				runner = function() { 
					thread name="LDEV-3914" {}
					return "success"
				}
				expect( runner() ).toBe("success");
			});

			it( title="checking with thread statement inside a lambda function", body=function() {
				try {
					local.result = _InternalRequest(
						template : "#createURI("LDEV3914")#/test.cfm"
					).filecontent;
				}
				catch(any e) {
					result = e.message;
				}
				expect( trim(result) ).toBe("success");
			});
		});
	}
	
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI& calledName;
	}
}
