component extends="org.lucee.cfml.test.LuceeTestCase" skip=true{
	
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-3951", function() {
			it( title="Inline comment in tag-in-script syntax", body=function( currentSpec ){
				try {
					var result = "success";
					local.result = _InternalRequest(
						template : "#createURI("LDEV3951")#\LDEV3951.cfm"
					);
				}
				catch(any e) {
					result = e.message;
				}
				expect(result).toBe("success");
			});
		});
	}
	
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}