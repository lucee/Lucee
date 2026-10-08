component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax,thread" {
	
	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-3914", function() {
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

			it( title="checking with thread statement inside a lambda function in a component", body=function() {
				expect( new LDEV3914.Lambda().run() ).toBe( "thread inside a lambda" );
			});
		});
	}
	
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI& calledName;
	}
}
