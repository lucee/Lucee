component extends="org.lucee.cfml.test.LuceeTestCase" skip=true{

	function run( testResults , testBox ) {
		describe( title="Test case for LDEV-3809", body=function() {
			it(title="java.lang.VerifyError in dataAdd arguments with hyphen(-)", body = function( currentSpec ) {
				try {
					var res = _InternalRequest(
						template:createURI("LDEV3809/LDEV3809.cfm")
					).filecontent.trim();
				}
				catch(any e) {
					var res = e.type;
				}
				expect(res).toBe("dateAdd a result:01/01/2022");
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}