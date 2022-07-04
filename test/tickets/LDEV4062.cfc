component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, textbox ) {
		describe("Testcase for LDEV-4062", function() {
			it(title="checking lambda expression without the body({})", body=function( currentSpec ) {
				try {
					local.result = _internalRequest(
						template : "#createURI("LDEV4062")#/LDEV4062.cfm"
					).filecontent;
				}
				catch(any e){
					local.result = e.message;
				}
				expect(trim(result)).toBe("lambda expression works without body({})");
			});
		});
	}
	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}