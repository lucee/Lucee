component extends = "org.lucee.cfml.test.LuceeTestCase" skip="true" {

	function run ( testResults , testBox ) {
		describe("Testcase for LDEV-3963", function() {	
			it( title="Using Colon (:) in tag syntax function attribute", body=function( currentSpec ) {
				expect(structKeyExists(getMetadata( testfunc ),"secured:api")).tobeTrue();
			});

			it( title="Using Colon (:) in script syntax function attribute", body=function( currentSpec ) {
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

			it( title="Using colon (:) in script syntax component function attribute", body=function( currentSpec ) {
				try {
					var res = new LDEV3963.test();
					var result = structKeyExists(getMetadata(res).functions[2],"ACCESS:REMOTE")
				}
				catch(any e) {
					var result = e.message;
				}
				expect(result).tobe("true");
			});

			it( title="Using equal(=) in script syntax component function attribute", body=function( currentSpec ) {
				try {
					var result = new LDEV3963.test().get();
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

	```
	<cffunction name="testfunc" access="private" secured:api>
			
	</cffunction>

	```
}