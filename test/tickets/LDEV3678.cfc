component extends="org.lucee.cfml.test.LuceeTestCase" labels="http" {
	function beforeAll() {
		variables.httpbin = server.getTestService("httpbin");
		variables.hasHttpbin = structCount(variables.httpbin) > 0;
		variables.httpbinUrl = variables.hasHttpbin ? "http://#variables.httpbin.server#:#variables.httpbin.port#" : "";
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV-3678", function() {
			it( title="checking charset key in cfhttp result with empty charset", body=function( currentSpec ){
				http url="https://raw.githubusercontent.com/lucee/Lucee/6.0/test/functions/images/lucee.png" result="local.result";
				expect(result).toHaveKey("charset");
			});
			it( title="checking charset key in cfhttp result with charset=UTF-8", body=function( currentSpec ){
				if ( !variables.hasHttpbin ) return;
				http url="#variables.httpbinUrl#/encoding/utf8" result="local.result";
				expect(result).toHaveKey("charset");   
				expect(uCase(result.charset)).toBe("UTF-8"); 
			});
		});
	}
}