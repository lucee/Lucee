component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run ( testResults , testBox ) {
		describe("Testcase for LDEV-3963", function() {	
			it( title="Using Colon(:) in tag syntax function attribute", body=function( currentSpec ) {
				expect(structKeyExists(getMetadata( testfunc ),"secured:api")).tobeTrue();
			});

			it( title="Using Colon(:) in script syntax function attribute", body=function( currentSpec ) {
				try {
					var result =_InternalRequest(
						template : "#createURI("LDEV3963")#\LDEV3963.cfm"
					).filecontent;
				}
				catch(any e) {
					var result = e.message;
				}
				expect(result).tobe("true");
			});	

		});
	}

	private string function createURI(string calledName) {
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

	// Using Colon(:) in tag based syntax function attribute 
	```
	<cffunction name="testfunc" access="private" secured:api>
			
	</cffunction>

	```
}