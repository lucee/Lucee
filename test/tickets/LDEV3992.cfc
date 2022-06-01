component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults , testBox ) {
		describe( "test case for LDEV-3992", function() {
			it(title = "Checking tag-in-script syntax without the semi-colon", body = function( currentSpec ) {
				try {
					var result = _InternalRequest(
						template:"#createURI("LDEV3992")#/LDEV3992.cfm"
					).filecontent;
				}
				catch(any e) {
					var result = e.message; 
				}
				expect(trim(result)).toBe("success");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}