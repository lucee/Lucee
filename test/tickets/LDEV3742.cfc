component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true{
	function run( testResults, textbox ) {
		describe("testcase for LDEV-3742", function() {
			variables.path = "http://"&cgi.SERVER_NAME & getDirectoryFromPath(cgi.SCRIPT_NAME)&"LDEV3742/test.cfc";
			it(title="Checking Content-Type of cfcontent with type attribute", body=function( currentSpec ) {
				cfhttp( url = "#path#?method=withType", result="local.output");
				expect(findNoCase("image/png", output.responseheader["Content-Type"]) > 0).toBeTrue();
			});
			it(title="Checking Content-Type of cfcontent without type attribute", body=function( currentSpec ) {
				cfhttp( url = "#path#?method=withoutType", result="local.output");
				expect(findNoCase("image/png", output.responseheader["Content-Type"]) > 0).toBeTrue();
			});
		});
	}
}
