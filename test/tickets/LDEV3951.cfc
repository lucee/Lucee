component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function run( testResults, testBox ) {
		describe("Testcase for LDEV-3951, inline comments", function() {
			it( title="Inline comment in tag-in-script syntax", body=function( currentSpec ){
				try {
					var result = "success";
					local.result = _InternalRequest(
						template : "#createURI("LDEV3951")#\LDEV3951_cfdump.cfm"
					);
					result=result.filecontent;
				}
				catch(any e) {
					result = e.stacktrace;
				}
				expect(result).toBe("success");
			});

			it( title="Inline comment in tag-in-script syntax dump", body=function( currentSpec ){
				try {
					var result = "success";
					local.result = _InternalRequest(
						template : "#createURI("LDEV3951")#\LDEV3951_dump.cfm"
					);
					result=result.filecontent;
				}
				catch(any e) {
					result = e.stacktrace;
				}
				expect(result).toBe("success");
			});

			it( title="Inline comment in tag-in-script syntax dump tag", body=function( currentSpec ){
				try {
					var result = "success";
					local.result = _InternalRequest(
						template : "#createURI("LDEV3951")#\LDEV3951_dump_tag.cfm"
					);
					result=result.filecontent;
				}
				catch(any e) {
					result = e.stacktrace;
				}
				expect(result).toBe("success");
			});
		});

		describe("Testcase for LDEV-5310, no space after silent before bracket causes exception", function() {

			it( title="Inline comment in tag-in-script syntax dump", body=function( currentSpec ){
				try {
					var result = "success";
					local.result = _InternalRequest(
						template : "#createURI("LDEV3951")#\LDEV3951_dump_no_space.cfm"
					);
					result=result.filecontent;
				}
				catch(any e) {
					result = e.stacktrace;
				}
				expect(result).toBe("success");
			});
		});
	}
	
	private string function createURI(string calledName, boolean contract=true){
		var base = getDirectoryFromPath( getCurrentTemplatePath() );
		var baseURI = contract ? contractPath( base ) : "/test/#listLast(base,"\/")#";
		return baseURI & "/" & calledName;
	}
}